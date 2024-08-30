/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.storage.sql.feature;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Locale;
import java.util.logging.Level;
import java.util.concurrent.locks.ReadWriteLock;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.opengis.util.NameFactory;
import org.opengis.util.NameSpace;
import org.opengis.util.GenericName;
import org.apache.sis.setup.GeometryLibrary;
import org.apache.sis.geometry.wrapper.Geometries;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.IllegalNameException;
import org.apache.sis.storage.InternalDataStoreException;
import org.apache.sis.storage.event.StoreListeners;
import org.apache.sis.storage.sql.ResourceDefinition;
import org.apache.sis.storage.sql.postgis.Postgres;
import org.apache.sis.metadata.sql.privy.Dialect;
import org.apache.sis.metadata.sql.privy.Reflection;
import org.apache.sis.util.iso.DefaultNameFactory;
import org.apache.sis.util.resources.ResourceInternationalString;


/**
 * Helper methods for creating {@code FeatureType}s from database structure.
 * An instance of this class is created temporarily when starting the analysis
 * of a database structure, and discarded after the analysis is finished.
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 */
public final class Analyzer {
    /**
     * Information about the spatial database to analyze.
     */
    public final Database<?> database;

    /**
     * A cache of statements for fetching spatial information such as geometry columns or SRID.
     * May be {@code null} if the database is not a spatial database, e.g. because the geometry
     * table has not been found.
     */
    InfoStatements spatialInformation;

    /**
     * Information about the database as a whole.
     * Used for fetching tables, columns, primary keys <i>etc.</i>
     */
    final DatabaseMetaData metadata;

    /**
     * The factory for creating {@code FeatureType} names.
     */
    final NameFactory nameFactory;

    /**
     * A pool of strings read from database metadata. Those strings are mostly catalog, schema and column names.
     * The same names are repeated often (in primary keys, foreigner keys, <i>etc.</i>), and using a pool allows
     * us to replace equal character strings by the same {@link String} instances.
     *
     * @see #getUniqueString(ResultSet, String)
     */
    private final Map<String,String> uniqueStrings;

    /**
     * The string to insert before wildcard characters ({@code '_'} or {@code '%'}) to escape.
     * This is used by {@link #escape(String)} before to pass argument values (e.g. table name)
     * to {@link DatabaseMetaData} methods expecting a pattern.
     */
    final String escape;

    /**
     * The "TABLE" and "VIEW" keywords for table types, with unsupported keywords omitted.
     */
    private final String[] tableTypes;

    /**
     * Names of tables to ignore. This set includes at least the tables defined by the spatial
     * schema standard from storing geometry columns, spatial reference systems, <i>etc.</i>
     */
    private final Set<String> ignoredTables;

    /**
     * All tables created by analysis of the database structure. A {@code null} value means that the table
     * is in process of being created. This may happen if there is cyclic dependencies between tables.
     */
    private final Map<GenericName,Table> featureTables;

    /**
     * Warnings found while analyzing a database structure. Duplicated warnings are omitted.
     */
    private final Set<ResourceInternationalString> warnings;

    /**
     * The last catalog and schema used for creating {@link #namespace}.
     * Used for determining if {@link #namespace} is still valid.
     */
    private transient String catalog, schema;

    /**
     * The namespace created with {@link #catalog} and {@link #schema}.
     *
     * @see #namespace(String, String)
     */
    private transient NameSpace namespace;

    /**
     * User-specified modification to the features, or {@code null} if none.
     */
    SchemaModifier customizer;

    /**
     * Creates a new analyzer for a spatial database.
     * Callers shall invoke {@link #analyze analyze(…)} after this method.
     *
     * @param  source         provider of (pooled) connections to the database.
     * @param  metadata       value of {@code connection.getMetaData()} (provided because already known by caller).
     * @param  geomLibrary    the factory to use for creating geometric objects.
     * @param  contentLocale  the locale to use for international texts to write in the database, or {@code null} for default.
     * @param  listeners      where to send warnings.
     * @param  locks          the read/write locks, or {@code null} if none.
     * @throws SQLException if a database error occurred while reading metadata.
     * @throws DataStoreException if a logical error occurred while analyzing the database structure.
     */
    public Analyzer(final DataSource source, final DatabaseMetaData metadata, final GeometryLibrary geomLibrary,
                    final Locale contentLocale, final StoreListeners listeners, final ReadWriteLock locks)
            throws Exception
    {
        this.metadata      = metadata;
        this.escape        = metadata.getSearchStringEscape();
        this.nameFactory   = DefaultNameFactory.provider();
        this.featureTables = new HashMap<>();
        this.uniqueStrings = new HashMap<>();
        this.warnings      = new LinkedHashSet<>();
        /*
         * Finds the keyword used for identifying tables and views.
         * Derby, HSQLDB and PostgreSQL uses the "TABLE" type, but H2 uses "BASE TABLE".
         */
        final Set<String> types = new HashSet<>(4);
        try (ResultSet reflect = metadata.getTableTypes()) {
            while (reflect.next()) {
                final String type = reflect.getString(Reflection.TABLE_TYPE);
                if ("TABLE".equalsIgnoreCase(type) || "VIEW".equalsIgnoreCase(type) || "BASE TABLE".equalsIgnoreCase(type)) {
                    types.add(type);
                }
            }
        }
        tableTypes = types.toArray(String[]::new);
        /*
         * Creates the database, then detect the spatial schema. Detects also the catalog and schema names.
         * It needs to be done before to search for feature tables, because the latter may require accesses
         * to the SPATIAL_REF_SYS table. For example, if the search consists in reading the "gpkg_contents"
         * table.
         */
        final Geometries<?> g = Geometries.factory(geomLibrary);
        final Dialect dialect = Dialect.guess(metadata);
        switch (dialect) {
            case POSTGRESQL: database = new Postgres<>(source, metadata, dialect, g, contentLocale, listeners, locks); break;
            default:         database = new Database<>(source, metadata, dialect, g, contentLocale, listeners, locks); break;
        }
        ignoredTables = database.detectSpatialSchema(metadata, tableTypes);
    }

    /**
     * Collects the names of all tables specified by user, ignoring standard tables such as {@code SPATIAL_REF_SYS}.
     * This method requires a list of tables to include in the model, but this list should not include dependencies.
     * This method will follow foreigner keys automatically.
     *
     * <h4>Prerequisites</h4>
     * The {@link #spatialInformation} (if available) and {@link #customizer} (if any) fields should
     * be set before to invoke this method. Those fields are optional, they may be {@code null}.
     *
     * @param  tableNames  qualified name of the tables. Specified by users at construction time.
     * @param  queries     additional resources associated to SQL queries. Specified by users at construction time.
     * @throws Exception if a <abbr>SQL</abbr> error or logical error occurred.
     *
     * @see #finish()
     */
    final List<Table> findFeatureTables(final GenericName[] tableNames, final ResourceDefinition[] queries) throws Exception {
        final var declared = new LinkedHashSet<TableReference>();
        for (final GenericName tableName : tableNames) {
            final String[] names = TableReference.splitName(tableName);
            try (ResultSet reflect = metadata.getTables(names[2], names[1], names[0], tableTypes)) {
                while (reflect.next()) {
                    final String table = getUniqueString(reflect, Reflection.TABLE_NAME);
                    if (ignoredTables.contains(table)) {
                        continue;
                    }
                    declared.add(new TableReference(
                            getUniqueString(reflect, Reflection.TABLE_CAT),
                            getUniqueString(reflect, Reflection.TABLE_SCHEM), table,
                            getUniqueString(reflect, Reflection.REMARKS)));
                }
            }
        }
        /*
         * At this point we got the list of tables requested by the user. Now create the Table objects for each
         * specified name. During this iteration, we may discover new tables to analyze because of dependencies
         * (foreigner keys).
         */
        final List<Table> tableList;
        tableList = new ArrayList<>(tableNames.length);
        for (final TableReference reference : declared) {
            // Adds only the table explicitly required by the user.
            tableList.add(table(reference, reference.getName(this), null));
        }
        /*
         * Add queries if any.
         */
        for (final ResourceDefinition resource : queries) {
            // Optional value should always be present in this context.
            tableList.add(query(resource.getName(), resource.getQuery().get()));
        }
        return tableList;
    }

    /**
     * Reads a string from the given result set and return a unique instance of that string.
     * This method should be invoked only for {@code String} instances that are going to be
     * stored in {@link Table} or {@link Relation} structures; there is no point to invoke
     * this method for example before to parse the string as a boolean.
     *
     * @param  reflect  the result set from which to read a string.
     * @param  column   the column to read.
     * @return the value in the given column, returned as a unique string.
     */
    final String getUniqueString(final ResultSet reflect, final String column) throws SQLException {
        String value = reflect.getString(column);
        if (value != null) {
            final String p = uniqueStrings.putIfAbsent(value, value);
            if (p != null) value = p;
        }
        return value;
    }

    /**
     * Returns a namespace for the given catalog and schema names, or {@code null} if all arguments are null.
     * The namespace sets the name separator to {@code '.'} instead of {@code ':'}.
     */
    final NameSpace namespace(final String catalog, final String schema) {
        if (!Objects.equals(this.schema, schema) || !Objects.equals(this.catalog, catalog)) {
            if (schema != null) {
                final GenericName name;
                if (catalog == null) {
                    name = nameFactory.createLocalName(null, schema);
                } else {
                    name = nameFactory.createGenericName(null, catalog, schema);
                }
                namespace = nameFactory.createNameSpace(name, Map.of("separator", "."));
            } else {
                namespace = null;
            }
            this.catalog = catalog;
            this.schema  = schema;
        }
        return namespace;
    }

    /**
     * Returns the table of the given name if it exists, or creates it otherwise.
     * This method may be invoked recursively if the table to create is a dependency
     * of another table. If a cyclic dependency is detected, then this method returns
     * {@code null} for one of the tables.
     *
     * @param  id            identification of the table to create.
     * @param  name          the value of {@code id.getName(analyzer)}
     *                       (as an argument for avoiding re-computation when already known by the caller).
     * @param  dependencyOf  if the analyzed table is imported/exported by foreigner keys,
     *                       the table that "contains" this table. Otherwise {@code null}.
     * @return the table, or {@code null} if there is a cyclic dependency and
     *         the table of the given name is already in process of being created.
     */
    final Table table(final TableReference id, final GenericName name, final TableReference dependencyOf) throws Exception {
        Table table = featureTables.get(name);
        if (table == null && !featureTables.containsKey(name)) {
            featureTables.put(name, null);                       // Mark the feature as in process of being created.
            table = new Table(database, new TableAnalyzer(this, id, dependencyOf), null);
            if (featureTables.put(name, table) != null) {
                // Should never happen. If thrown, we have a bug (e.g. synchronization) in this package.
                throw new InternalDataStoreException(internalError());
            }
        }
        return table;
    }

    /**
     * Creates a virtual table for the given query. If a table already exists for the given name,
     * then an {@link IllegalNameException} is thrown.
     *
     * @param  name   name of the resource.
     * @param  query  the query to execute.
     * @return the virtual table for the given query.
     */
    private Table query(final GenericName name, final String query) throws Exception {
        final Table table = new Table(database, new QueryAnalyzer(this, name, query, null), query);
        if (!featureTables.containsKey(name) && featureTables.put(name, table) == null) {
            return table;
        }
        throw new IllegalNameException(resources().getString(Resources.Keys.NameAlreadyUsed_1, name));
    }

    /**
     * Returns the localized resources for warnings and error messages.
     */
    final Resources resources() {
        return Resources.forLocale(database.listeners.getLocale());
    }

    /**
     * Returns a message for unexpected errors. Those errors are caused by a bug in this
     * {@code org.apache.sis.storage.sql.feature} package instead of a database issue.
     */
    final String internalError() {
        return resources().getString(Resources.Keys.InternalError);
    }

    /**
     * Reports a warning. Duplicated warnings will be ignored.
     *
     * @param  key       one of {@link Resources.Keys} values.
     * @param  argument  the value to substitute to {0} tag in the warning message.
     */
    private void warning(final short key, final Object argument) {
        warnings.add(Resources.formatInternational(key, argument));
    }

    /**
     * Invoked after we finished to create all tables. This method flushes the warnings
     * (omitting duplicated warnings), then returns all tables including dependencies.
     */
    final Collection<Table> finish() throws DataStoreException {
        for (final Table table : featureTables.values()) {
            table.setDeferredSearchTables(this, featureTables);
        }
        for (final ResourceInternationalString warning : warnings) {
            database.log(warning.toLogRecord(Level.WARNING));
        }
        return featureTables.values();
    }

    /**
     * Initializes the value getter on the given column.
     * This method shall be invoked only after geometry columns have been identified.
     */
    final ValueGetter<?> setValueGetter(final Column column) {
        ValueGetter<?> getter = database.getMapping(column);
        if (getter == null) {
            getter = database.getDefaultMapping();
            warning(Resources.Keys.UnknownType_1, column.typeName);
        }
        column.valueGetter = getter;
        return getter;
    }
}
