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
package org.apache.sis.storage.esri;

import java.net.URL;
import java.io.IOException;
import org.apache.sis.util.Workaround;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.StorageConnector;

// Test dependencies
import static org.junit.jupiter.api.Assertions.*;
import org.apache.sis.storage.test.CoverageReadConsistency;


/**
 * Test consistency of read operations in random domains of a BIP (Band Interleaved by Line) file.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class BILConsistencyTest extends CoverageReadConsistency<RawRasterStore> {
    /**
     * Opens the test file to be used for all tests.
     *
     * @throws IOException if an error occurred while opening the file.
     * @throws DataStoreException if an error occurred while reading the file.
     */
    public BILConsistencyTest() throws IOException, DataStoreException {
        super(openFile());
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    @Workaround(library="JDK", version="1.7")
    private static RawRasterStore openFile() throws IOException, DataStoreException {
        final URL url = BIPConsistencyTest.class.getResource("BIL.raw");
        assertNotNull(url, "Test file not found.");
        return new RawRasterStore(null, new StorageConnector(url));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    @Override
    protected GridCoverageResource resource() throws DataStoreException {
        return store;
    }
}
