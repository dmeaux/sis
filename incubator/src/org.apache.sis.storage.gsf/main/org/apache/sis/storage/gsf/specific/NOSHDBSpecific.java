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
package org.apache.sis.storage.gsf.specific;

import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import org.apache.sis.storage.gsf.GSF;
import org.apache.sis.storage.gsf.StructClass;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class NOSHDBSpecific extends StructClass {


    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
        GSF.C_SHORT.withName("type_code"),
        GSF.C_SHORT.withName("carto_code")
    ).withName("t_gsfNOSHDBSpecific");

    public NOSHDBSpecific(MemorySegment struct) {
        super(struct);
    }

    public NOSHDBSpecific(SegmentAllocator allocator) {
        super(allocator);
    }

    @Override
    protected MemoryLayout getLayout() {
        return LAYOUT;
    }

    private static final OfShort type_codeLAYOUT = (OfShort)LAYOUT.select(groupElement("type_code"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned short type_code
     * }
     */
    public static final OfShort type_codeLAYOUT() {
        return type_codeLAYOUT;
    }

    private static final long type_code$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned short type_code
     * }
     */
    public static final long type_code$offset() {
        return type_code$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned short type_code
     * }
     */
    public static short type_code(MemorySegment struct) {
        return struct.get(type_codeLAYOUT, type_code$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned short type_code
     * }
     */
    public static void type_code(MemorySegment struct, short fieldValue) {
        struct.set(type_codeLAYOUT, type_code$OFFSET, fieldValue);
    }

    private static final OfShort carto_codeLAYOUT = (OfShort)LAYOUT.select(groupElement("carto_code"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned short carto_code
     * }
     */
    public static final OfShort carto_codeLAYOUT() {
        return carto_codeLAYOUT;
    }

    private static final long carto_code$OFFSET = 2;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned short carto_code
     * }
     */
    public static final long carto_code$offset() {
        return carto_code$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned short carto_code
     * }
     */
    public static short carto_code(MemorySegment struct) {
        return struct.get(carto_codeLAYOUT, carto_code$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned short carto_code
     * }
     */
    public static void carto_code(MemorySegment struct, short fieldValue) {
        struct.set(carto_codeLAYOUT, carto_code$OFFSET, fieldValue);
    }

}

