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
package org.apache.sis.storage.earthobservation;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.StorageConnector;
import org.apache.sis.storage.landsat.LandsatStoreProvider;


/**
 * Parses Landsat metadata as {@linkplain org.apache.sis.metadata.iso.DefaultMetadata ISO 19115 Metadata} object.
 * Landsat data are distributed as a collection of TIFF files, together with a single
 * text file like below:
 *
 * {@preformat text
 * GROUP = L1_METADATA_FILE
 *   GROUP = METADATA_FILE_INFO
 *     ORIGIN = "Image courtesy of the U.S. Geological Survey"
 *     REQUEST_ID = "0501403126384_00011"
 *     LANDSAT_SCENE_ID = "LC81230522014071LGN00"
 *     FILE_DATE = 2014-03-12T06:06:35Z
 *     STATION_ID = "LGN"
 *     PROCESSING_SOFTWARE_VERSION = "LPGS_2.3.0"
 *   END_GROUP = METADATA_FILE_INFO
 *   GROUP = PRODUCT_METADATA
 *     DATA_TYPE = "L1T"
 *     ELEVATION_SOURCE = "GLS2000"
 *     OUTPUT_FORMAT = "GEOTIFF"
 *     SPACECRAFT_ID = "LANDSAT_8"
 *     SENSOR_ID = "OLI_TIRS"
 *     etc...
 * }
 *
 * This class reads the content from the given input until the first occurrence of the {@code END} keyword.
 * Lines beginning with the {@code #} character (ignoring spaces) are treated as comment lines and ignored.
 *
 * @author  Thi Phuong Hao Nguyen (VNSC)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.1
 * @since   0.8
 * @module
 *
 * @deprecated Moved to the {@link org.apache.sis.storage.landsat} package.
 */
@Deprecated
public class LandsatStore extends org.apache.sis.storage.landsat.LandsatStore {
    /**
     * Creates a new Landsat store from the given file, URL, stream or character reader.
     * This constructor invokes {@link StorageConnector#closeAllExcept(Object)},
     * keeping open only the needed resource.
     *
     * @param  provider   the factory that created this {@code DataStore} instance, or {@code null} if unspecified.
     * @param  connector  information about the storage (URL, stream, reader instance, <i>etc</i>).
     * @throws DataStoreException if an error occurred while opening the Landsat file.
     */
    public LandsatStore(final LandsatStoreProvider provider, final StorageConnector connector) throws DataStoreException {
        super(provider, connector);
    }
}
