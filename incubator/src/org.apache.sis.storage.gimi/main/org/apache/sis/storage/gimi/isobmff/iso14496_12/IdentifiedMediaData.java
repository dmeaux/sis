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
package org.apache.sis.storage.gimi.isobmff.iso14496_12;

import java.io.IOException;
import org.apache.sis.storage.gimi.isobmff.Box;
import org.apache.sis.storage.gimi.isobmff.ISOBMFFReader;


/**
 * Container: File
 *
 * @author Johann Sorel (Geomatys)
 */
public final class IdentifiedMediaData extends Box {

    public static final String FCC = "imda";

    public int identifier;
    public byte[] data;

    @Override
    public void readProperties(ISOBMFFReader reader) throws IOException {
        identifier = reader.channel.readInt();
        reader.channel.seek(boxOffset + size);
    }

    public byte[] getData() throws IOException {
        synchronized (reader) {
            reader.channel.seek(payloadOffset + 4);
            final long nb = (boxOffset + size) - payloadOffset - 4;
            return reader.channel.readBytes(Math.toIntExact(nb));
        }
    }

}
