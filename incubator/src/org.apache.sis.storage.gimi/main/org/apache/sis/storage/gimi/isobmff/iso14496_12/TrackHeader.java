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
import org.apache.sis.io.stream.ChannelDataInput;
import org.apache.sis.storage.gimi.isobmff.FullBox;
import org.apache.sis.storage.gimi.isobmff.ISOBMFFReader;


/**
 * Container: TrackBox
 *
 * @author Johann Sorel (Geomatys)
 */
public final class TrackHeader extends FullBox {

    public static final String FCC = "tkhd";

    @Override
    public void readProperties(ISOBMFFReader reader) throws IOException {
        super.readProperties(reader);
        //TODO
    }

}
