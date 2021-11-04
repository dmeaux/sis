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
package org.apache.sis.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.OptionalLong;
import java.io.Serializable;
import javax.measure.Quantity;
import javax.measure.quantity.Length;
import org.opengis.util.GenericName;
import org.opengis.geometry.Envelope;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.feature.builder.PropertyTypeBuilder;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.internal.feature.FeatureExpression;
import org.apache.sis.internal.filter.SortByComparator;
import org.apache.sis.internal.storage.Resources;
import org.apache.sis.filter.DefaultFilterFactory;
import org.apache.sis.filter.Optimization;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Classes;
import org.apache.sis.util.collection.Containers;
import org.apache.sis.util.iso.Names;

// Branch-dependent imports
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.filter.Filter;
import org.apache.sis.filter.Expression;
import org.apache.sis.internal.geoapi.filter.Literal;
import org.apache.sis.internal.geoapi.filter.ValueReference;
import org.apache.sis.internal.geoapi.filter.SortBy;
import org.apache.sis.internal.geoapi.filter.SortProperty;


/**
 * Definition of filtering to apply for fetching a subset of {@link FeatureSet}.
 * This query mimics {@code SQL SELECT} statements using OGC Filter and Expressions.
 * Information stored in this query can be used directly with {@link java.util.stream.Stream} API.
 *
 * <h2>Terminology</h2>
 * This class uses relational database terminology:
 * <ul>
 *   <li>A <cite>selection</cite> is a filter choosing the features instances to include in the subset.
 *       In relational databases, a feature instances are mapped to table rows.</li>
 *   <li>A <cite>projection</cite> (not to be confused with map projection) is the set of feature property to keep.
 *       In relational databases, feature properties are mapped to table columns.</li>
 * </ul>
 *
 * <h2>Optional values</h2>
 * All aspects of this query are optional and initialized to "none".
 * Unless otherwise specified, all methods accept a null argument or can return a null value, which means "none".
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 * @since   1.1
 * @module
 */
public class FeatureQuery extends Query implements Cloneable, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -5841189659773611160L;

    /**
     * Sentinel limit value for queries of unlimited length.
     * This value applies to the {@link #limit} field.
     */
    private static final long UNLIMITED = -1;

    /**
     * The properties to retrieve, or {@code null} if all properties shall be included in the query.
     * In a database, "properties" are table columns.
     * Subset of columns is called <cite>projection</cite> in relational database terminology.
     *
     * @see #getProjection()
     * @see #setProjection(NamedExpression[])
     */
    private NamedExpression[] projection;

    /**
     * The filter for trimming feature instances.
     * In a database, "feature instances" are table rows.
     * Subset of rows is called <cite>selection</cite> in relational database terminology.
     *
     * @see #getSelection()
     * @see #setSelection(Filter)
     */
    private Filter<? super AbstractFeature> selection;

    /**
     * The number of feature instances to skip from the beginning.
     * This is zero if there is no instance to skip.
     *
     * @see #getOffset()
     * @see #setOffset(long)
     * @see java.util.stream.Stream#skip(long)
     */
    private long skip;

    /**
     * The maximum number of feature instances contained in the {@code FeatureSet}.
     * This is {@link #UNLIMITED} if there is no limit.
     *
     * @see #getLimit()
     * @see #setLimit(long)
     * @see java.util.stream.Stream#limit(long)
     */
    private long limit;

    /**
     * The expressions to use for sorting the feature instances.
     *
     * @see #getSortBy()
     * @see #setSortBy(SortBy)
     */
    private SortBy<AbstractFeature> sortBy;

    /**
     * Hint used by resources to optimize returned features.
     * Different stores make use of vector tiles of different scales.
     * A {@code null} value means to query data at their full resolution.
     *
     * @see #getLinearResolution()
     * @see #setLinearResolution(Quantity)
     */
    private Quantity<Length> linearResolution;

    /**
     * Creates a new query applying no filter.
     */
    public FeatureQuery() {
        limit = UNLIMITED;
    }

    /**
     * Sets the properties to retrieve by their names. This convenience method wraps the
     * given names in {@link ValueReference} expressions without alias and delegates to
     * {@link #setProjection(NamedExpression...)}.
     *
     * @param  properties  properties to retrieve, or {@code null} to retrieve all properties.
     * @throws IllegalArgumentException if a property is duplicated.
     */
    @Override
    public void setProjection(final String... properties) {
        NamedExpression[] wrappers = null;
        if (properties != null) {
            final DefaultFilterFactory<AbstractFeature,?,?> ff = DefaultFilterFactory.forFeatures();
            wrappers = new NamedExpression[properties.length];
            for (int i=0; i<wrappers.length; i++) {
                final String p = properties[i];
                ArgumentChecks.ensureNonNullElement("properties", i, p);
                wrappers[i] = new NamedExpression(ff.property(p));
            }
        }
        setProjection(wrappers);
    }

    /**
     * Sets the properties to retrieve, or {@code null} if all properties shall be included in the query.
     * This convenience method wraps the given expression in {@link NamedExpression}s without alias and
     * delegates to {@link #setProjection(NamedExpression...)}.
     *
     * @param  properties  properties to retrieve, or {@code null} to retrieve all properties.
     * @throws IllegalArgumentException if a property is duplicated.
     */
    @SafeVarargs
    public final void setProjection(final Expression<? super AbstractFeature, ?>... properties) {
        NamedExpression[] wrappers = null;
        if (properties != null) {
            wrappers = new NamedExpression[properties.length];
            for (int i=0; i<wrappers.length; i++) {
                final Expression<? super AbstractFeature, ?> e = properties[i];
                ArgumentChecks.ensureNonNullElement("properties", i, e);
                wrappers[i] = new NamedExpression(e);
            }
        }
        setProjection(wrappers);
    }

    /**
     * Sets the properties to retrieve, or {@code null} if all properties shall be included in the query.
     * A query column may use a simple or complex expression and an alias to create a new type of property
     * in the returned features.
     *
     * <p>This is equivalent to the column names in the {@code SELECT} clause of a SQL statement.
     * Subset of columns is called <cite>projection</cite> in relational database terminology.</p>
     *
     * @param  properties  properties to retrieve, or {@code null} to retrieve all properties.
     * @throws IllegalArgumentException if a property or an alias is duplicated.
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setProjection(NamedExpression... properties) {
        if (properties != null) {
            ArgumentChecks.ensureNonEmpty("properties", properties);
            properties = properties.clone();
            final Map<Object,Integer> uniques = new LinkedHashMap<>(Containers.hashMapCapacity(properties.length));
            for (int i=0; i<properties.length; i++) {
                final NamedExpression c = properties[i];
                ArgumentChecks.ensureNonNullElement("properties", i, c);
                final Object key = c.alias != null ? c.alias : c.expression;
                final Integer p = uniques.putIfAbsent(key, i);
                if (p != null) {
                    throw new IllegalArgumentException(Resources.format(Resources.Keys.DuplicatedQueryProperty_3, key, p, i));
                }
            }
        }
        this.projection = properties;
    }

    /**
     * Returns the properties to retrieve, or {@code null} if all properties shall be included in the query.
     * This is the expressions specified in the last call to {@link #setProjection(NamedExpression[])}.
     * The default value is null.
     *
     * @return properties to retrieve, or {@code null} to retrieve all feature properties.
     */
    public NamedExpression[] getProjection() {
        return (projection != null) ? projection.clone() : null;
    }

    /**
     * Sets the approximate area of feature instances to include in the subset.
     * This convenience method creates a filter that checks if the bounding box
     * of the feature's {@code "sis:geometry"} property interacts with the given envelope.
     *
     * @param  domain  the approximate area of interest, or {@code null} if none.
     */
    @Override
    public void setSelection(final Envelope domain) {
        Filter<? super AbstractFeature> filter = null;
        if (domain != null) {
            final DefaultFilterFactory<AbstractFeature,Object,?> ff = DefaultFilterFactory.forFeatures();
            filter = ff.bbox(ff.property(AttributeConvention.GEOMETRY), domain);
        }
        setSelection(filter);
    }

    /**
     * Sets a filter for trimming feature instances.
     * Features that do not pass the filter are discarded.
     * Discarded features are not counted for the {@linkplain #setLimit(long) query limit}.
     *
     * @param  selection  the filter, or {@code null} if none.
     */
    public void setSelection(final Filter<? super AbstractFeature> selection) {
        this.selection = selection;
    }

    /**
     * Returns the filter for trimming feature instances.
     * This is the value specified in the last call to {@link #setSelection(Filter)}.
     * The default value is {@code null}, which means that no filtering is applied.
     *
     * @return the filter, or {@code null} if none.
     */
    public Filter<? super AbstractFeature> getSelection() {
        return selection;
    }

    /**
     * Sets the number of feature instances to skip from the beginning.
     * Offset and limit are often combined to obtain paging.
     * The offset can not be negative.
     *
     * <p>Note that setting this property can be costly on parallelized streams.
     * See {@link java.util.stream.Stream#skip(long)} for more information.</p>
     *
     * @param  skip  the number of feature instances to skip from the beginning.
     */
    public void setOffset(final long skip) {
        ArgumentChecks.ensurePositive("skip", skip);
        this.skip = skip;
    }

    /**
     * Returns the number of feature instances to skip from the beginning.
     * This is the value specified in the last call to {@link #setOffset(long)}.
     * The default value is zero, which means that no features are skipped.
     *
     * @return the number of feature instances to skip from the beginning.
     */
    public long getOffset() {
        return skip;
    }

    /**
     * Removes any limit defined by {@link #setLimit(long)}.
     */
    public void setUnlimited() {
        limit = UNLIMITED;
    }

    /**
     * Set the maximum number of feature instances contained in the {@code FeatureSet}.
     * Offset and limit are often combined to obtain paging.
     *
     * <p>Note that setting this property can be costly on parallelized streams.
     * See {@link java.util.stream.Stream#limit(long)} for more information.</p>
     *
     * @param  limit  maximum number of feature instances contained in the {@code FeatureSet}.
     */
    public void setLimit(final long limit) {
        ArgumentChecks.ensurePositive("limit", limit);
        this.limit = limit;
    }

    /**
     * Returns the maximum number of feature instances contained in the {@code FeatureSet}.
     * This is the value specified in the last call to {@link #setLimit(long)}.
     *
     * @return maximum number of feature instances contained in the {@code FeatureSet}, or empty if none.
     */
    public OptionalLong getLimit() {
        return (limit >= 0) ? OptionalLong.of(limit) : OptionalLong.empty();
    }

    /**
     * Sets the expressions to use for sorting the feature instances.
     * {@code SortBy} objects are used to order the {@code Feature} instances returned by the {@link FeatureSet}.
     * {@code SortBy} clauses are applied in declaration order, like SQL.
     *
     * @param  properties  expressions to use for sorting the feature instances,
     *                     or {@code null} or an empty array if none.
     *
     * @todo Not yet in public API. Pending publication of {@link SortProperty} interface.
     */
    @SafeVarargs
    final void setSortBy(final SortProperty<AbstractFeature>... properties) {
        SortBy<AbstractFeature> sortBy = null;
        if (properties != null) {
            sortBy = SortByComparator.create(properties);
        }
        setSortBy(sortBy);
    }

    /**
     * Sets the expressions to use for sorting the feature instances.
     * {@code SortBy} objects are used to order the {@code Feature} instances returned by the {@link FeatureSet}.
     *
     * @param  sortBy  expressions to use for sorting the feature instances, or {@code null} if none.
     *
     * @todo Not yet in public API. Pending publication of {@link SortProperty} interface.
     */
    final void setSortBy(final SortBy<AbstractFeature> sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Returns the expressions to use for sorting the feature instances.
     * This is the value specified in the last call to {@link #setSortBy(SortBy)}.
     *
     * @return expressions to use for sorting the feature instances, or {@code null} if none.
     *
     * @todo Not yet in public API. Pending publication of {@link SortProperty} interface.
     */
    final SortBy<AbstractFeature> getSortBy() {
        return sortBy;
    }

    /**
     * Sets the desired spatial resolution of geometries.
     * This property is an optional hint; resources may ignore it.
     *
     * @param  linearResolution  desired spatial resolution, or {@code null} for full resolution.
     */
    public void setLinearResolution(final Quantity<Length> linearResolution) {
        this.linearResolution = linearResolution;
    }

    /**
     * Returns the desired spatial resolution of geometries.
     * A {@code null} value means that data are queried at their full resolution.
     *
     * @return  desired spatial resolution, or {@code null} for full resolution.
     */
    public Quantity<Length> getLinearResolution() {
        return linearResolution;
    }

    /**
     * An expression to be retrieved by a {@code Query}, together with the name to assign to it.
     * In relational database terminology, subset of columns is called <cite>projection</cite>.
     * Columns can be given to the {@link FeatureQuery#setProjection(NamedExpression[])} method.
     */
    public static class NamedExpression implements Serializable {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -6919525113513842514L;

        /**
         * The literal, value reference or more complex expression to be retrieved by a {@code Query}.
         * Never {@code null}.
         */
        public final Expression<? super AbstractFeature, ?> expression;

        /**
         * The name to assign to the expression result, or {@code null} if unspecified.
         */
        public final GenericName alias;

        /**
         * Creates a new column with the given expression and no name.
         *
         * @param expression  the literal, value reference or expression to be retrieved by a {@code Query}.
         */
        public NamedExpression(final Expression<? super AbstractFeature, ?> expression) {
            ArgumentChecks.ensureNonNull("expression", expression);
            this.expression = expression;
            this.alias = null;
        }

        /**
         * Creates a new column with the given expression and the given name.
         *
         * @param expression  the literal, value reference or expression to be retrieved by a {@code Query}.
         * @param alias       the name to assign to the expression result, or {@code null} if unspecified.
         */
        public NamedExpression(final Expression<? super AbstractFeature, ?> expression, final GenericName alias) {
            ArgumentChecks.ensureNonNull("expression", expression);
            this.expression = expression;
            this.alias = alias;
        }

        /**
         * Creates a new column with the given expression and the given name.
         * This constructor creates a {@link org.opengis.util.LocalName} from the given string.
         *
         * @param expression  the literal, value reference or expression to be retrieved by a {@code Query}.
         * @param alias       the name to assign to the expression result, or {@code null} if unspecified.
         */
        public NamedExpression(final Expression<? super AbstractFeature, ?> expression, final String alias) {
            ArgumentChecks.ensureNonNull("expression", expression);
            this.expression = expression;
            this.alias = (alias != null) ? Names.createLocalName(null, null, alias) : null;
        }

        /**
         * Adds in the given builder the type of results computed by this column.
         *
         * @param  column     index of this column. Used for error message only.
         * @param  valueType  the type of features to be evaluated by the expression in this column.
         * @param  addTo      where to add the type of properties evaluated by expression in this column.
         * @throws IllegalArgumentException if this method can operate only on some feature types
         *         and the given type is not one of them.
         * @throws IllegalArgumentException if this method can not determine the result type of the expression
         *         in this column. It may be because that expression is backed by an unsupported implementation.
         *
         * @see FeatureQuery#expectedType(DefaultFeatureType)
         */
        final void expectedType(final int column, final DefaultFeatureType valueType, final FeatureTypeBuilder addTo) {
            final PropertyTypeBuilder resultType = FeatureExpression.expectedType(expression, valueType, addTo);
            if (resultType == null) {
                throw new IllegalArgumentException(Resources.format(Resources.Keys.InvalidExpression_2,
                            expression.getFunctionName().toInternationalString(), column));
            }
            if (alias != null && !alias.equals(resultType.getName())) {
                resultType.setName(alias);
            }
        }

        /**
         * Returns a hash code value for this column.
         *
         * @return a hash code value.
         */
        @Override
        public int hashCode() {
            return 37 * expression.hashCode() + Objects.hashCode(alias);
        }

        /**
         * Compares this column with the given object for equality.
         *
         * @param  obj  the object to compare with this column.
         * @return whether the two objects are equal.
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj != null && getClass() == obj.getClass()) {
                final NamedExpression other = (NamedExpression) obj;
                return expression.equals(other.expression) && Objects.equals(alias, other.alias);
            }
            return false;
        }

        /**
         * Returns a string representation of this column for debugging purpose.
         *
         * @return a string representation of this column.
         */
        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(getClass().getSimpleName()).append('[');      // Class name without enclosing class.
            appendTo(buffer);
            return buffer.append(']').toString();
        }

        /**
         * Appends a string representation of this column in the given buffer.
         */
        final void appendTo(final StringBuilder buffer) {
            if (expression instanceof Literal<?,?>) {
                buffer.append('“').append(((Literal<?,?>) expression).getValue()).append('”');
            } else if (expression instanceof ValueReference<?,?>) {
                buffer.append(((ValueReference<?,?>) expression).getXPath());
            } else {
                buffer.append(Classes.getShortClassName(expression));   // Class name with enclosing class if any.
            }
            if (alias != null) {
                buffer.append(" AS “").append(alias).append('”');
            }
        }
    }

    /**
     * Applies this query on the given feature set.
     * This method is invoked by the default implementation of {@link FeatureSet#subset(Query)}.
     * The default implementation executes the query using the default {@link java.util.stream.Stream} methods.
     * Queries executed by this method may not benefit from accelerations provided for example by databases.
     * This method should be used only as a fallback when the query can not be executed natively
     * by {@link FeatureSet#subset(Query)}.
     *
     * <p>The returned {@code FeatureSet} does not cache the resulting {@code Feature} instances;
     * the query is processed on every call to the {@link FeatureSet#features(boolean)} method.</p>
     *
     * @param  source  the set of features to filter, sort or process.
     * @return a view over the given feature set containing only the filtered feature instances.
     * @throws DataStoreException if an error occurred during creation of the subset.
     *
     * @see FeatureSet#subset(Query)
     * @see CoverageQuery#execute(GridCoverageResource)
     *
     * @since 1.2
     */
    protected FeatureSet execute(final FeatureSet source) throws DataStoreException {
        ArgumentChecks.ensureNonNull("source", source);
        final FeatureQuery query = clone();
        if (query.selection != null) {
            final Optimization optimization = new Optimization();
            optimization.setFeatureType(source.getType());
            query.selection = optimization.apply(query.selection);
        }
        return new FeatureSubset(source, query);
    }

    /**
     * Returns the type of values evaluated by this query when executed on features of the given type.
     *
     * @param  valueType  the type of features to be evaluated by the expressions in this query.
     * @return type resulting from expressions evaluation (never null).
     * @throws IllegalArgumentException if this method can operate only on some feature types
     *         and the given type is not one of them.
     * @throws IllegalArgumentException if this method can not determine the result type of an expression
     *         in this query. It may be because that expression is backed by an unsupported implementation.
     */
    final DefaultFeatureType expectedType(final DefaultFeatureType valueType) {
        if (projection == null) {
            return valueType;           // All columns included: result is of the same type.
        }
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder().setName(valueType.getName());
        for (int i=0; i<projection.length; i++) {
            projection[i].expectedType(i, valueType, ftb);
        }
        return ftb.build();
    }

    /**
     * Returns a clone of this query.
     *
     * @return a clone of this query.
     */
    @Override
    public FeatureQuery clone() {
        /*
         * Implementation note: no need to clone the arrays. It is safe to share the same array instances
         * because this class does not modify them and does not return them directly to the user.
         */
        try {
            return (FeatureQuery) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a hash code value for this query.
     *
     * @return a hash value for this query.
     */
    @Override
    public int hashCode() {
        return 97 * Arrays.hashCode(projection) + 31 * selection.hashCode()
              + 7 * Objects.hashCode(sortBy) + Long.hashCode(limit ^ skip)
              + 3 * Objects.hashCode(linearResolution);
    }

    /**
     * Compares this query with the given object for equality.
     *
     * @param  obj  the object to compare with this query.
     * @return whether the two objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            final FeatureQuery other = (FeatureQuery) obj;
            return skip  == other.skip &&
                   limit == other.limit &&
                   selection.equals(other.selection) &&
                   Arrays .equals(projection,       other.projection) &&
                   Objects.equals(sortBy,           other.sortBy) &&
                   Objects.equals(linearResolution, other.linearResolution);
        }
        return false;
    }

    /**
     * Returns a textual representation of this query for debugging purposes.
     * The default implementation returns a string that looks like an SQL Select query.
     *
     * @return textual representation of this query.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(80);
        sb.append("SELECT ");
        if (projection != null) {
            for (int i=0; i<projection.length; i++) {
                if (i != 0) sb.append(", ");
                projection[i].appendTo(sb);
            }
        } else {
            sb.append('*');
        }
        if (selection != Filter.include()) {
            sb.append(" WHERE ").append(selection);
        }
        if (sortBy != null) {
            String separator = " ORDER BY ";
            for (final SortProperty<AbstractFeature> p : sortBy.getSortProperties()) {
                sb.append(separator);
                separator = ", ";
                sb.append(p.getValueReference().getXPath()).append(' ').append(p.getSortOrder());
            }
        }
        if (linearResolution != null) {
            sb.append(" RESOLUTION ").append(linearResolution);
        }
        if (limit != UNLIMITED) {
            sb.append(" LIMIT ").append(limit);
        }
        if (skip != 0) {
            sb.append(" OFFSET ").append(skip);
        }
        return sb.toString();
    }
}