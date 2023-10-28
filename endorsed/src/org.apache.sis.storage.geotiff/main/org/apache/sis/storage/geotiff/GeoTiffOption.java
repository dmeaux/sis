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
package org.apache.sis.storage.geotiff;

import org.apache.sis.setup.OptionKey;
import org.apache.sis.io.stream.InternalOptionKey;


/**
 * Characteristics of the GeoTIFF file to write.
 * The options can control, for example, the maximal size and number of images that can be stored in a TIFF file.
 * See {@link #OPTION_KEY} for an usage example.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.5
 *
 * @see GeoTiffStore#getOptions()
 *
 * @since 1.5
 */
public enum GeoTiffOption {
    /**
     * The Big TIFF extension (non-standard).
     * When this option is absent (which is the default), the standard TIFF format as defined by Adobe is used.
     * That standard uses the addressable space of 32-bits integers, which allows a maximal file size of about 4 GB.
     * When the {@code BIG_TIFF} option is present, the addressable space of 64-bits integers is used.
     * The BigTIFF format is non-standard and files written with this option may not be read by all TIFF readers.
     */
    BIG_TIFF;

    // TODO: COG, SPARSE.

    /**
     * The key for declaring GeoTIFF options at store creation time.
     * For writing a BigTIFF file, the following code can be used:
     *
     * {@snippet lang="java" :
     *     var file = Path.of("my_output_file.tiff");
     *     var connector = new StorageConnector(file);
     *     var options = new GeoTiffOption[] {GeoTiffOption.BIG_TIFF};
     *     connector.setOption(GeoTiffOption.OPTION_KEY, options);
     *     DataStore ds = DataStores.open(c);
     *     }
     */
    public static final OptionKey<GeoTiffOption[]> OPTION_KEY = new InternalOptionKey<>("TIFF_OPTIONS", GeoTiffOption[].class);
}
