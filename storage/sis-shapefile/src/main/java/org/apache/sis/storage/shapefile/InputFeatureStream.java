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
package org.apache.sis.storage.shapefile;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLFeatureNotSupportedException;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.SQLShapefileNotFoundException;
import org.apache.sis.internal.shapefile.ShapefileByteReader;
import org.apache.sis.internal.shapefile.jdbc.*;
import org.apache.sis.internal.shapefile.jdbc.connection.DBFConnection;
import org.apache.sis.internal.shapefile.jdbc.metadata.DBFDatabaseMetaData;
import org.apache.sis.internal.shapefile.jdbc.resultset.*;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLIllegalParameterException;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLInvalidStatementException;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLUnsupportedParsingFeatureException;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;
import org.apache.sis.storage.DataStoreClosedException;
import org.apache.sis.feature.AbstractFeature;

/**
 * Input Stream of features.
 *
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
public class InputFeatureStream extends InputStream {
    /** Dedicated connection to DBF. */
    private DBFConnection connection;

    /** Statement. */
    private DBFStatement stmt;

    /** ResultSet. */
    private DBFRecordBasedResultSet rs;

    /** SQL Statement executed. */
    private String sql;

    /** Marks the end of file. */
    private boolean endOfFile;

    /** Shapefile. */
    private File shapefile;

    /** Database file. */
    private File databaseFile;

    /** Type of the features contained in this shapefile. */
    private DefaultFeatureType featuresType;

    /** Shapefile reader. */
    private ShapefileByteReader shapefileReader;

    /**
     * Create an input stream of features over a connection.
     * @param shpfile Shapefile.
     * @param dbaseFile Database file.
     * @throws InvalidShapefileFormatException if the shapefile format is invalid.
     * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
     * @throws ShapefileNotFoundException if the shapefile has not been found.
     * @throws DbaseFileNotFoundException if the database file has not been found.
     */
    public InputFeatureStream(File shpfile, File dbaseFile) throws InvalidDbaseFileFormatException, InvalidShapefileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException {
        try {
            connection = (DBFConnection)new DBFDriver().connect(dbaseFile.getAbsolutePath(), null);
            sql = MessageFormat.format("SELECT * FROM {0}", dbaseFile.getName());
            shapefile = shpfile;
            databaseFile = dbaseFile;

            shapefileReader = new ShapefileByteReader(shapefile, databaseFile);
            featuresType = shapefileReader.getFeaturesType();

            try {
                executeQuery();
            }
            catch(SQLConnectionClosedException e) {
                // This would be an internal trouble because in this function (at least) it should be open.
                throw new RuntimeException(e.getMessage(), e);
            }
            catch(SQLInvalidStatementException e) {
                // This would be an internal trouble because if any SQL statement is executed for the dbase file initialization, it should has a correct syntax or grammar.
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        catch(SQLInvalidDbaseFileFormatException ex) {
            // Promote this exception to an DataStoreException compatible exception.
            throw new InvalidDbaseFileFormatException(ex.getMessage(), ex);
        }
        catch(SQLDbaseFileNotFoundException ex) {
            // Promote this exception to an DataStoreException compatible exception.
            throw new DbaseFileNotFoundException(ex.getMessage(), ex);
        }
        catch(SQLShapefileNotFoundException ex) {
            // Promote this exception to an DataStoreException compatible exception.
            throw new ShapefileNotFoundException(ex.getMessage(), ex);
        }
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() {
        throw new UnsupportedOperationException("InputFeatureStream doesn't allow the use of read(). Use readFeature() instead.");
    }

    /**
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() {
        throw new UnsupportedOperationException("InputFeatureStream doesn't allow the use of available(). Use readFeature() will return null when feature are no more available.");
    }

    /**
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() {
        rs.close();
        stmt.close();
        connection.close();
    }

    /**
     * Read next feature responding to the SQL query.
     * @return Feature, null if no more feature is available.
     * @throws DataStoreClosedException if the current connection used to query the shapefile has been closed.
     * @throws DataStoreQueryException if the statement used to query the shapefile content is incorrect.
     * @throws DataStoreQueryResultException if the shapefile results cause a trouble (wrong format, for example).
     * @throws InvalidShapefileFormatException if the shapefile structure shows a problem.
     */
    public AbstractFeature readFeature() throws DataStoreClosedException, DataStoreQueryException, DataStoreQueryResultException, InvalidShapefileFormatException {
        try {
            return internalReadFeature();
        }
        catch(SQLConnectionClosedException e) {
            throw new DataStoreClosedException(e.getMessage(), e);
        }
        catch(SQLNotNumericException e) {
            throw new DataStoreQueryResultException(e.getMessage(), e);
        }
        catch(SQLNotDateException e) {
            throw new DataStoreQueryResultException(e.getMessage(), e);
        }
        catch(java.sql.SQLException e) {
            throw new DataStoreQueryException(e.getMessage(), e);
        }
    }

    /**
     * Read next feature responding to the SQL query.
     * @return Feature, null if no more feature is available.
     * @throws SQLNotNumericException if a field expected numeric isn't.
     * @throws SQLNotDateException if a field expected of date kind, isn't.
     * @throws SQLNoSuchFieldException if a field doesn't exist.
     * @throws SQLIllegalParameterException if a parameter is illegal in the query.
     * @throws SQLInvalidStatementException if the SQL statement is invalid.
     * @throws SQLConnectionClosedException if the connection is closed.
     * @throws SQLUnsupportedParsingFeatureException if a SQL ability is not currently available through this driver.
     * @throws SQLFeatureNotSupportedException if a SQL ability is not currently available through this driver.
     * @throws InvalidShapefileFormatException if the shapefile format is invalid.
     */
    private AbstractFeature internalReadFeature() throws SQLConnectionClosedException, SQLInvalidStatementException, SQLIllegalParameterException, SQLNoSuchFieldException, SQLUnsupportedParsingFeatureException, SQLNotNumericException, SQLNotDateException, SQLFeatureNotSupportedException, InvalidShapefileFormatException {
        try {
            if (endOfFile) {
                return null;
            }

            if (rs.next() == false) {
                endOfFile = true;
                return null;
            }

            AbstractFeature feature = featuresType.newInstance();
            shapefileReader.completeFeature(feature);
            DBFDatabaseMetaData metadata = (DBFDatabaseMetaData)connection.getMetaData();

            DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = (DBFBuiltInMemoryResultSetForColumnsListing)metadata.getColumns(null, null, null, null);
            try {
                while(rsDatabase.next()) {
                    String fieldName = rsDatabase.getString("COLUMN_NAME");
                    Object fieldValue = rs.getObject(fieldName);

                    // FIXME To allow features to be filled again, the values are converted to String again : feature should allow any kind of data.
                    String stringValue;

                    if (fieldValue == null) {
                        stringValue = null;
                    }
                    else {
                        if (fieldValue instanceof Integer || fieldValue instanceof Long) {
                            stringValue = MessageFormat.format("{0,number,#0}", fieldValue); // Avoid thousand separator.
                        }
                        else {
                            if (fieldValue instanceof Double || fieldValue instanceof Float) {
                                // Avoid thousand separator.
                                DecimalFormat df = new DecimalFormat();
                                df.setGroupingUsed(false);
                                stringValue = df.format(fieldValue);
                            }
                            else
                                stringValue = fieldValue.toString();
                        }
                    }

                    feature.setPropertyValue(fieldName, stringValue);
                }

                return feature;
            }
            catch(SQLNoResultException e) {
                // This an internal trouble, if it occurs.
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                rsDatabase.close();
            }
        }
        catch(SQLNoResultException e) {
            // We are trying to prevent this. If it occurs, we have an internal problem.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Execute the wished SQL query.
     * @throws SQLConnectionClosedException if the connection is closed.
     * @throws SQLInvalidStatementException if the given SQL Statement is invalid.
     */
    private void executeQuery() throws SQLConnectionClosedException, SQLInvalidStatementException {
        stmt = (DBFStatement)connection.createStatement();
        rs = (DBFRecordBasedResultSet)stmt.executeQuery(sql);
    }
}