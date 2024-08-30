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

import java.lang.invoke.*;
import java.lang.foreign.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import org.apache.sis.storage.gsf.GSF;
import org.apache.sis.storage.gsf.StructClass;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class Reson7100Specific extends StructClass {

    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
        GSF.C_INT.withName("protocol_version"),
        GSF.C_INT.withName("device_id"),
        MemoryLayout.sequenceLayout(16, GSF.C_CHAR).withName("reserved_1"),
        GSF.C_INT.withName("major_serial_number"),
        GSF.C_INT.withName("minor_serial_number"),
        GSF.C_INT.withName("ping_number"),
        GSF.C_INT.withName("multi_ping_seq"),
        GSF.C_DOUBLE.withName("frequency"),
        GSF.C_DOUBLE.withName("sample_rate"),
        GSF.C_DOUBLE.withName("receiver_bandwdth"),
        GSF.C_DOUBLE.withName("tx_pulse_width"),
        GSF.C_INT.withName("tx_pulse_type_id"),
        GSF.C_INT.withName("tx_pulse_envlp_id"),
        GSF.C_DOUBLE.withName("tx_pulse_envlp_param"),
        GSF.C_INT.withName("tx_pulse_reserved"),
        MemoryLayout.paddingLayout(4),
        GSF.C_DOUBLE.withName("max_ping_rate"),
        GSF.C_DOUBLE.withName("ping_period"),
        GSF.C_DOUBLE.withName("range"),
        GSF.C_DOUBLE.withName("power"),
        GSF.C_DOUBLE.withName("gain"),
        GSF.C_INT.withName("control_flags"),
        GSF.C_INT.withName("projector_id"),
        GSF.C_DOUBLE.withName("projector_steer_angl_vert"),
        GSF.C_DOUBLE.withName("projector_steer_angl_horz"),
        GSF.C_DOUBLE.withName("projector_beam_wdth_vert"),
        GSF.C_DOUBLE.withName("projector_beam_wdth_horz"),
        GSF.C_DOUBLE.withName("projector_beam_focal_pt"),
        GSF.C_INT.withName("projector_beam_weighting_window_type"),
        GSF.C_INT.withName("projector_beam_weighting_window_param"),
        GSF.C_INT.withName("transmit_flags"),
        GSF.C_INT.withName("hydrophone_id"),
        GSF.C_INT.withName("receiving_beam_weighting_window_type"),
        GSF.C_INT.withName("receiving_beam_weighting_window_param"),
        GSF.C_INT.withName("receive_flags"),
        MemoryLayout.paddingLayout(4),
        GSF.C_DOUBLE.withName("receive_beam_width"),
        GSF.C_DOUBLE.withName("range_filt_min"),
        GSF.C_DOUBLE.withName("range_filt_max"),
        GSF.C_DOUBLE.withName("depth_filt_min"),
        GSF.C_DOUBLE.withName("depth_filt_max"),
        GSF.C_DOUBLE.withName("absorption"),
        GSF.C_DOUBLE.withName("sound_velocity"),
        GSF.C_DOUBLE.withName("spreading"),
        GSF.C_CHAR.withName("raw_data_from_7027"),
        MemoryLayout.sequenceLayout(15, GSF.C_CHAR).withName("reserved_2"),
        GSF.C_CHAR.withName("sv_source"),
        GSF.C_CHAR.withName("layer_comp_flag"),
        MemoryLayout.sequenceLayout(8, GSF.C_CHAR).withName("reserved_3"),
        MemoryLayout.paddingLayout(6)
    ).withName("t_gsfReson7100Specific");

    public Reson7100Specific(MemorySegment struct) {
        super(struct);
    }

    public Reson7100Specific(SegmentAllocator allocator) {
        super(allocator);
    }

    @Override
    protected MemoryLayout getLayout() {
        return LAYOUT;
    }

    private static final OfInt protocol_versionLAYOUT = (OfInt)LAYOUT.select(groupElement("protocol_version"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int protocol_version
     * }
     */
    public static final OfInt protocol_versionLAYOUT() {
        return protocol_versionLAYOUT;
    }

    private static final long protocol_version$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int protocol_version
     * }
     */
    public static final long protocol_version$offset() {
        return protocol_version$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int protocol_version
     * }
     */
    public static int protocol_version(MemorySegment struct) {
        return struct.get(protocol_versionLAYOUT, protocol_version$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int protocol_version
     * }
     */
    public static void protocol_version(MemorySegment struct, int fieldValue) {
        struct.set(protocol_versionLAYOUT, protocol_version$OFFSET, fieldValue);
    }

    private static final OfInt device_idLAYOUT = (OfInt)LAYOUT.select(groupElement("device_id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int device_id
     * }
     */
    public static final OfInt device_idLAYOUT() {
        return device_idLAYOUT;
    }

    private static final long device_id$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int device_id
     * }
     */
    public static final long device_id$offset() {
        return device_id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int device_id
     * }
     */
    public static int device_id(MemorySegment struct) {
        return struct.get(device_idLAYOUT, device_id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int device_id
     * }
     */
    public static void device_id(MemorySegment struct, int fieldValue) {
        struct.set(device_idLAYOUT, device_id$OFFSET, fieldValue);
    }

    private static final SequenceLayout reserved_1LAYOUT = (SequenceLayout)LAYOUT.select(groupElement("reserved_1"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static final SequenceLayout reserved_1LAYOUT() {
        return reserved_1LAYOUT;
    }

    private static final long reserved_1$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static final long reserved_1$offset() {
        return reserved_1$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static MemorySegment reserved_1(MemorySegment struct) {
        return struct.asSlice(reserved_1$OFFSET, reserved_1LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static void reserved_1(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, reserved_1$OFFSET, reserved_1LAYOUT.byteSize());
    }

    private static long[] reserved_1$DIMS = { 16 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static long[] reserved_1$dimensions() {
        return reserved_1$DIMS;
    }
    private static final VarHandle reserved_1$ELEM_HANDLE = reserved_1LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static byte reserved_1(MemorySegment struct, long index0) {
        return (byte)reserved_1$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * unsigned char reserved_1[16]
     * }
     */
    public static void reserved_1(MemorySegment struct, long index0, byte fieldValue) {
        reserved_1$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final OfInt major_serial_numberLAYOUT = (OfInt)LAYOUT.select(groupElement("major_serial_number"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int major_serial_number
     * }
     */
    public static final OfInt major_serial_numberLAYOUT() {
        return major_serial_numberLAYOUT;
    }

    private static final long major_serial_number$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int major_serial_number
     * }
     */
    public static final long major_serial_number$offset() {
        return major_serial_number$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int major_serial_number
     * }
     */
    public static int major_serial_number(MemorySegment struct) {
        return struct.get(major_serial_numberLAYOUT, major_serial_number$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int major_serial_number
     * }
     */
    public static void major_serial_number(MemorySegment struct, int fieldValue) {
        struct.set(major_serial_numberLAYOUT, major_serial_number$OFFSET, fieldValue);
    }

    private static final OfInt minor_serial_numberLAYOUT = (OfInt)LAYOUT.select(groupElement("minor_serial_number"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int minor_serial_number
     * }
     */
    public static final OfInt minor_serial_numberLAYOUT() {
        return minor_serial_numberLAYOUT;
    }

    private static final long minor_serial_number$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int minor_serial_number
     * }
     */
    public static final long minor_serial_number$offset() {
        return minor_serial_number$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int minor_serial_number
     * }
     */
    public static int minor_serial_number(MemorySegment struct) {
        return struct.get(minor_serial_numberLAYOUT, minor_serial_number$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int minor_serial_number
     * }
     */
    public static void minor_serial_number(MemorySegment struct, int fieldValue) {
        struct.set(minor_serial_numberLAYOUT, minor_serial_number$OFFSET, fieldValue);
    }

    private static final OfInt ping_numberLAYOUT = (OfInt)LAYOUT.select(groupElement("ping_number"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int ping_number
     * }
     */
    public static final OfInt ping_numberLAYOUT() {
        return ping_numberLAYOUT;
    }

    private static final long ping_number$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int ping_number
     * }
     */
    public static final long ping_number$offset() {
        return ping_number$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int ping_number
     * }
     */
    public static int ping_number(MemorySegment struct) {
        return struct.get(ping_numberLAYOUT, ping_number$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int ping_number
     * }
     */
    public static void ping_number(MemorySegment struct, int fieldValue) {
        struct.set(ping_numberLAYOUT, ping_number$OFFSET, fieldValue);
    }

    private static final OfInt multi_ping_seqLAYOUT = (OfInt)LAYOUT.select(groupElement("multi_ping_seq"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int multi_ping_seq
     * }
     */
    public static final OfInt multi_ping_seqLAYOUT() {
        return multi_ping_seqLAYOUT;
    }

    private static final long multi_ping_seq$OFFSET = 36;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int multi_ping_seq
     * }
     */
    public static final long multi_ping_seq$offset() {
        return multi_ping_seq$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int multi_ping_seq
     * }
     */
    public static int multi_ping_seq(MemorySegment struct) {
        return struct.get(multi_ping_seqLAYOUT, multi_ping_seq$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int multi_ping_seq
     * }
     */
    public static void multi_ping_seq(MemorySegment struct, int fieldValue) {
        struct.set(multi_ping_seqLAYOUT, multi_ping_seq$OFFSET, fieldValue);
    }

    private static final OfDouble frequencyLAYOUT = (OfDouble)LAYOUT.select(groupElement("frequency"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double frequency
     * }
     */
    public static final OfDouble frequencyLAYOUT() {
        return frequencyLAYOUT;
    }

    private static final long frequency$OFFSET = 40;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double frequency
     * }
     */
    public static final long frequency$offset() {
        return frequency$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double frequency
     * }
     */
    public static double frequency(MemorySegment struct) {
        return struct.get(frequencyLAYOUT, frequency$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double frequency
     * }
     */
    public static void frequency(MemorySegment struct, double fieldValue) {
        struct.set(frequencyLAYOUT, frequency$OFFSET, fieldValue);
    }

    private static final OfDouble sample_rateLAYOUT = (OfDouble)LAYOUT.select(groupElement("sample_rate"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double sample_rate
     * }
     */
    public static final OfDouble sample_rateLAYOUT() {
        return sample_rateLAYOUT;
    }

    private static final long sample_rate$OFFSET = 48;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double sample_rate
     * }
     */
    public static final long sample_rate$offset() {
        return sample_rate$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double sample_rate
     * }
     */
    public static double sample_rate(MemorySegment struct) {
        return struct.get(sample_rateLAYOUT, sample_rate$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double sample_rate
     * }
     */
    public static void sample_rate(MemorySegment struct, double fieldValue) {
        struct.set(sample_rateLAYOUT, sample_rate$OFFSET, fieldValue);
    }

    private static final OfDouble receiver_bandwdthLAYOUT = (OfDouble)LAYOUT.select(groupElement("receiver_bandwdth"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double receiver_bandwdth
     * }
     */
    public static final OfDouble receiver_bandwdthLAYOUT() {
        return receiver_bandwdthLAYOUT;
    }

    private static final long receiver_bandwdth$OFFSET = 56;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double receiver_bandwdth
     * }
     */
    public static final long receiver_bandwdth$offset() {
        return receiver_bandwdth$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double receiver_bandwdth
     * }
     */
    public static double receiver_bandwdth(MemorySegment struct) {
        return struct.get(receiver_bandwdthLAYOUT, receiver_bandwdth$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double receiver_bandwdth
     * }
     */
    public static void receiver_bandwdth(MemorySegment struct, double fieldValue) {
        struct.set(receiver_bandwdthLAYOUT, receiver_bandwdth$OFFSET, fieldValue);
    }

    private static final OfDouble tx_pulse_widthLAYOUT = (OfDouble)LAYOUT.select(groupElement("tx_pulse_width"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double tx_pulse_width
     * }
     */
    public static final OfDouble tx_pulse_widthLAYOUT() {
        return tx_pulse_widthLAYOUT;
    }

    private static final long tx_pulse_width$OFFSET = 64;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double tx_pulse_width
     * }
     */
    public static final long tx_pulse_width$offset() {
        return tx_pulse_width$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double tx_pulse_width
     * }
     */
    public static double tx_pulse_width(MemorySegment struct) {
        return struct.get(tx_pulse_widthLAYOUT, tx_pulse_width$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double tx_pulse_width
     * }
     */
    public static void tx_pulse_width(MemorySegment struct, double fieldValue) {
        struct.set(tx_pulse_widthLAYOUT, tx_pulse_width$OFFSET, fieldValue);
    }

    private static final OfInt tx_pulse_type_idLAYOUT = (OfInt)LAYOUT.select(groupElement("tx_pulse_type_id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_type_id
     * }
     */
    public static final OfInt tx_pulse_type_idLAYOUT() {
        return tx_pulse_type_idLAYOUT;
    }

    private static final long tx_pulse_type_id$OFFSET = 72;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_type_id
     * }
     */
    public static final long tx_pulse_type_id$offset() {
        return tx_pulse_type_id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_type_id
     * }
     */
    public static int tx_pulse_type_id(MemorySegment struct) {
        return struct.get(tx_pulse_type_idLAYOUT, tx_pulse_type_id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_type_id
     * }
     */
    public static void tx_pulse_type_id(MemorySegment struct, int fieldValue) {
        struct.set(tx_pulse_type_idLAYOUT, tx_pulse_type_id$OFFSET, fieldValue);
    }

    private static final OfInt tx_pulse_envlp_idLAYOUT = (OfInt)LAYOUT.select(groupElement("tx_pulse_envlp_id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_envlp_id
     * }
     */
    public static final OfInt tx_pulse_envlp_idLAYOUT() {
        return tx_pulse_envlp_idLAYOUT;
    }

    private static final long tx_pulse_envlp_id$OFFSET = 76;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_envlp_id
     * }
     */
    public static final long tx_pulse_envlp_id$offset() {
        return tx_pulse_envlp_id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_envlp_id
     * }
     */
    public static int tx_pulse_envlp_id(MemorySegment struct) {
        return struct.get(tx_pulse_envlp_idLAYOUT, tx_pulse_envlp_id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_envlp_id
     * }
     */
    public static void tx_pulse_envlp_id(MemorySegment struct, int fieldValue) {
        struct.set(tx_pulse_envlp_idLAYOUT, tx_pulse_envlp_id$OFFSET, fieldValue);
    }

    private static final OfDouble tx_pulse_envlp_paramLAYOUT = (OfDouble)LAYOUT.select(groupElement("tx_pulse_envlp_param"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double tx_pulse_envlp_param
     * }
     */
    public static final OfDouble tx_pulse_envlp_paramLAYOUT() {
        return tx_pulse_envlp_paramLAYOUT;
    }

    private static final long tx_pulse_envlp_param$OFFSET = 80;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double tx_pulse_envlp_param
     * }
     */
    public static final long tx_pulse_envlp_param$offset() {
        return tx_pulse_envlp_param$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double tx_pulse_envlp_param
     * }
     */
    public static double tx_pulse_envlp_param(MemorySegment struct) {
        return struct.get(tx_pulse_envlp_paramLAYOUT, tx_pulse_envlp_param$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double tx_pulse_envlp_param
     * }
     */
    public static void tx_pulse_envlp_param(MemorySegment struct, double fieldValue) {
        struct.set(tx_pulse_envlp_paramLAYOUT, tx_pulse_envlp_param$OFFSET, fieldValue);
    }

    private static final OfInt tx_pulse_reservedLAYOUT = (OfInt)LAYOUT.select(groupElement("tx_pulse_reserved"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_reserved
     * }
     */
    public static final OfInt tx_pulse_reservedLAYOUT() {
        return tx_pulse_reservedLAYOUT;
    }

    private static final long tx_pulse_reserved$OFFSET = 88;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_reserved
     * }
     */
    public static final long tx_pulse_reserved$offset() {
        return tx_pulse_reserved$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_reserved
     * }
     */
    public static int tx_pulse_reserved(MemorySegment struct) {
        return struct.get(tx_pulse_reservedLAYOUT, tx_pulse_reserved$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int tx_pulse_reserved
     * }
     */
    public static void tx_pulse_reserved(MemorySegment struct, int fieldValue) {
        struct.set(tx_pulse_reservedLAYOUT, tx_pulse_reserved$OFFSET, fieldValue);
    }

    private static final OfDouble max_ping_rateLAYOUT = (OfDouble)LAYOUT.select(groupElement("max_ping_rate"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double max_ping_rate
     * }
     */
    public static final OfDouble max_ping_rateLAYOUT() {
        return max_ping_rateLAYOUT;
    }

    private static final long max_ping_rate$OFFSET = 96;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double max_ping_rate
     * }
     */
    public static final long max_ping_rate$offset() {
        return max_ping_rate$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double max_ping_rate
     * }
     */
    public static double max_ping_rate(MemorySegment struct) {
        return struct.get(max_ping_rateLAYOUT, max_ping_rate$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double max_ping_rate
     * }
     */
    public static void max_ping_rate(MemorySegment struct, double fieldValue) {
        struct.set(max_ping_rateLAYOUT, max_ping_rate$OFFSET, fieldValue);
    }

    private static final OfDouble ping_periodLAYOUT = (OfDouble)LAYOUT.select(groupElement("ping_period"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double ping_period
     * }
     */
    public static final OfDouble ping_periodLAYOUT() {
        return ping_periodLAYOUT;
    }

    private static final long ping_period$OFFSET = 104;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double ping_period
     * }
     */
    public static final long ping_period$offset() {
        return ping_period$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double ping_period
     * }
     */
    public static double ping_period(MemorySegment struct) {
        return struct.get(ping_periodLAYOUT, ping_period$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double ping_period
     * }
     */
    public static void ping_period(MemorySegment struct, double fieldValue) {
        struct.set(ping_periodLAYOUT, ping_period$OFFSET, fieldValue);
    }

    private static final OfDouble rangeLAYOUT = (OfDouble)LAYOUT.select(groupElement("range"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double range
     * }
     */
    public static final OfDouble rangeLAYOUT() {
        return rangeLAYOUT;
    }

    private static final long range$OFFSET = 112;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double range
     * }
     */
    public static final long range$offset() {
        return range$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double range
     * }
     */
    public static double range(MemorySegment struct) {
        return struct.get(rangeLAYOUT, range$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double range
     * }
     */
    public static void range(MemorySegment struct, double fieldValue) {
        struct.set(rangeLAYOUT, range$OFFSET, fieldValue);
    }

    private static final OfDouble powerLAYOUT = (OfDouble)LAYOUT.select(groupElement("power"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double power
     * }
     */
    public static final OfDouble powerLAYOUT() {
        return powerLAYOUT;
    }

    private static final long power$OFFSET = 120;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double power
     * }
     */
    public static final long power$offset() {
        return power$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double power
     * }
     */
    public static double power(MemorySegment struct) {
        return struct.get(powerLAYOUT, power$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double power
     * }
     */
    public static void power(MemorySegment struct, double fieldValue) {
        struct.set(powerLAYOUT, power$OFFSET, fieldValue);
    }

    private static final OfDouble gainLAYOUT = (OfDouble)LAYOUT.select(groupElement("gain"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double gain
     * }
     */
    public static final OfDouble gainLAYOUT() {
        return gainLAYOUT;
    }

    private static final long gain$OFFSET = 128;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double gain
     * }
     */
    public static final long gain$offset() {
        return gain$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double gain
     * }
     */
    public static double gain(MemorySegment struct) {
        return struct.get(gainLAYOUT, gain$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double gain
     * }
     */
    public static void gain(MemorySegment struct, double fieldValue) {
        struct.set(gainLAYOUT, gain$OFFSET, fieldValue);
    }

    private static final OfInt control_flagsLAYOUT = (OfInt)LAYOUT.select(groupElement("control_flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int control_flags
     * }
     */
    public static final OfInt control_flagsLAYOUT() {
        return control_flagsLAYOUT;
    }

    private static final long control_flags$OFFSET = 136;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int control_flags
     * }
     */
    public static final long control_flags$offset() {
        return control_flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int control_flags
     * }
     */
    public static int control_flags(MemorySegment struct) {
        return struct.get(control_flagsLAYOUT, control_flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int control_flags
     * }
     */
    public static void control_flags(MemorySegment struct, int fieldValue) {
        struct.set(control_flagsLAYOUT, control_flags$OFFSET, fieldValue);
    }

    private static final OfInt projector_idLAYOUT = (OfInt)LAYOUT.select(groupElement("projector_id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int projector_id
     * }
     */
    public static final OfInt projector_idLAYOUT() {
        return projector_idLAYOUT;
    }

    private static final long projector_id$OFFSET = 140;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int projector_id
     * }
     */
    public static final long projector_id$offset() {
        return projector_id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int projector_id
     * }
     */
    public static int projector_id(MemorySegment struct) {
        return struct.get(projector_idLAYOUT, projector_id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int projector_id
     * }
     */
    public static void projector_id(MemorySegment struct, int fieldValue) {
        struct.set(projector_idLAYOUT, projector_id$OFFSET, fieldValue);
    }

    private static final OfDouble projector_steer_angl_vertLAYOUT = (OfDouble)LAYOUT.select(groupElement("projector_steer_angl_vert"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double projector_steer_angl_vert
     * }
     */
    public static final OfDouble projector_steer_angl_vertLAYOUT() {
        return projector_steer_angl_vertLAYOUT;
    }

    private static final long projector_steer_angl_vert$OFFSET = 144;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double projector_steer_angl_vert
     * }
     */
    public static final long projector_steer_angl_vert$offset() {
        return projector_steer_angl_vert$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double projector_steer_angl_vert
     * }
     */
    public static double projector_steer_angl_vert(MemorySegment struct) {
        return struct.get(projector_steer_angl_vertLAYOUT, projector_steer_angl_vert$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double projector_steer_angl_vert
     * }
     */
    public static void projector_steer_angl_vert(MemorySegment struct, double fieldValue) {
        struct.set(projector_steer_angl_vertLAYOUT, projector_steer_angl_vert$OFFSET, fieldValue);
    }

    private static final OfDouble projector_steer_angl_horzLAYOUT = (OfDouble)LAYOUT.select(groupElement("projector_steer_angl_horz"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double projector_steer_angl_horz
     * }
     */
    public static final OfDouble projector_steer_angl_horzLAYOUT() {
        return projector_steer_angl_horzLAYOUT;
    }

    private static final long projector_steer_angl_horz$OFFSET = 152;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double projector_steer_angl_horz
     * }
     */
    public static final long projector_steer_angl_horz$offset() {
        return projector_steer_angl_horz$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double projector_steer_angl_horz
     * }
     */
    public static double projector_steer_angl_horz(MemorySegment struct) {
        return struct.get(projector_steer_angl_horzLAYOUT, projector_steer_angl_horz$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double projector_steer_angl_horz
     * }
     */
    public static void projector_steer_angl_horz(MemorySegment struct, double fieldValue) {
        struct.set(projector_steer_angl_horzLAYOUT, projector_steer_angl_horz$OFFSET, fieldValue);
    }

    private static final OfDouble projector_beam_wdth_vertLAYOUT = (OfDouble)LAYOUT.select(groupElement("projector_beam_wdth_vert"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_vert
     * }
     */
    public static final OfDouble projector_beam_wdth_vertLAYOUT() {
        return projector_beam_wdth_vertLAYOUT;
    }

    private static final long projector_beam_wdth_vert$OFFSET = 160;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_vert
     * }
     */
    public static final long projector_beam_wdth_vert$offset() {
        return projector_beam_wdth_vert$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_vert
     * }
     */
    public static double projector_beam_wdth_vert(MemorySegment struct) {
        return struct.get(projector_beam_wdth_vertLAYOUT, projector_beam_wdth_vert$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_vert
     * }
     */
    public static void projector_beam_wdth_vert(MemorySegment struct, double fieldValue) {
        struct.set(projector_beam_wdth_vertLAYOUT, projector_beam_wdth_vert$OFFSET, fieldValue);
    }

    private static final OfDouble projector_beam_wdth_horzLAYOUT = (OfDouble)LAYOUT.select(groupElement("projector_beam_wdth_horz"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_horz
     * }
     */
    public static final OfDouble projector_beam_wdth_horzLAYOUT() {
        return projector_beam_wdth_horzLAYOUT;
    }

    private static final long projector_beam_wdth_horz$OFFSET = 168;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_horz
     * }
     */
    public static final long projector_beam_wdth_horz$offset() {
        return projector_beam_wdth_horz$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_horz
     * }
     */
    public static double projector_beam_wdth_horz(MemorySegment struct) {
        return struct.get(projector_beam_wdth_horzLAYOUT, projector_beam_wdth_horz$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double projector_beam_wdth_horz
     * }
     */
    public static void projector_beam_wdth_horz(MemorySegment struct, double fieldValue) {
        struct.set(projector_beam_wdth_horzLAYOUT, projector_beam_wdth_horz$OFFSET, fieldValue);
    }

    private static final OfDouble projector_beam_focal_ptLAYOUT = (OfDouble)LAYOUT.select(groupElement("projector_beam_focal_pt"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double projector_beam_focal_pt
     * }
     */
    public static final OfDouble projector_beam_focal_ptLAYOUT() {
        return projector_beam_focal_ptLAYOUT;
    }

    private static final long projector_beam_focal_pt$OFFSET = 176;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double projector_beam_focal_pt
     * }
     */
    public static final long projector_beam_focal_pt$offset() {
        return projector_beam_focal_pt$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double projector_beam_focal_pt
     * }
     */
    public static double projector_beam_focal_pt(MemorySegment struct) {
        return struct.get(projector_beam_focal_ptLAYOUT, projector_beam_focal_pt$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double projector_beam_focal_pt
     * }
     */
    public static void projector_beam_focal_pt(MemorySegment struct, double fieldValue) {
        struct.set(projector_beam_focal_ptLAYOUT, projector_beam_focal_pt$OFFSET, fieldValue);
    }

    private static final OfInt projector_beam_weighting_window_typeLAYOUT = (OfInt)LAYOUT.select(groupElement("projector_beam_weighting_window_type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_type
     * }
     */
    public static final OfInt projector_beam_weighting_window_typeLAYOUT() {
        return projector_beam_weighting_window_typeLAYOUT;
    }

    private static final long projector_beam_weighting_window_type$OFFSET = 184;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_type
     * }
     */
    public static final long projector_beam_weighting_window_type$offset() {
        return projector_beam_weighting_window_type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_type
     * }
     */
    public static int projector_beam_weighting_window_type(MemorySegment struct) {
        return struct.get(projector_beam_weighting_window_typeLAYOUT, projector_beam_weighting_window_type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_type
     * }
     */
    public static void projector_beam_weighting_window_type(MemorySegment struct, int fieldValue) {
        struct.set(projector_beam_weighting_window_typeLAYOUT, projector_beam_weighting_window_type$OFFSET, fieldValue);
    }

    private static final OfInt projector_beam_weighting_window_paramLAYOUT = (OfInt)LAYOUT.select(groupElement("projector_beam_weighting_window_param"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_param
     * }
     */
    public static final OfInt projector_beam_weighting_window_paramLAYOUT() {
        return projector_beam_weighting_window_paramLAYOUT;
    }

    private static final long projector_beam_weighting_window_param$OFFSET = 188;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_param
     * }
     */
    public static final long projector_beam_weighting_window_param$offset() {
        return projector_beam_weighting_window_param$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_param
     * }
     */
    public static int projector_beam_weighting_window_param(MemorySegment struct) {
        return struct.get(projector_beam_weighting_window_paramLAYOUT, projector_beam_weighting_window_param$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int projector_beam_weighting_window_param
     * }
     */
    public static void projector_beam_weighting_window_param(MemorySegment struct, int fieldValue) {
        struct.set(projector_beam_weighting_window_paramLAYOUT, projector_beam_weighting_window_param$OFFSET, fieldValue);
    }

    private static final OfInt transmit_flagsLAYOUT = (OfInt)LAYOUT.select(groupElement("transmit_flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int transmit_flags
     * }
     */
    public static final OfInt transmit_flagsLAYOUT() {
        return transmit_flagsLAYOUT;
    }

    private static final long transmit_flags$OFFSET = 192;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int transmit_flags
     * }
     */
    public static final long transmit_flags$offset() {
        return transmit_flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int transmit_flags
     * }
     */
    public static int transmit_flags(MemorySegment struct) {
        return struct.get(transmit_flagsLAYOUT, transmit_flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int transmit_flags
     * }
     */
    public static void transmit_flags(MemorySegment struct, int fieldValue) {
        struct.set(transmit_flagsLAYOUT, transmit_flags$OFFSET, fieldValue);
    }

    private static final OfInt hydrophone_idLAYOUT = (OfInt)LAYOUT.select(groupElement("hydrophone_id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int hydrophone_id
     * }
     */
    public static final OfInt hydrophone_idLAYOUT() {
        return hydrophone_idLAYOUT;
    }

    private static final long hydrophone_id$OFFSET = 196;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int hydrophone_id
     * }
     */
    public static final long hydrophone_id$offset() {
        return hydrophone_id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int hydrophone_id
     * }
     */
    public static int hydrophone_id(MemorySegment struct) {
        return struct.get(hydrophone_idLAYOUT, hydrophone_id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int hydrophone_id
     * }
     */
    public static void hydrophone_id(MemorySegment struct, int fieldValue) {
        struct.set(hydrophone_idLAYOUT, hydrophone_id$OFFSET, fieldValue);
    }

    private static final OfInt receiving_beam_weighting_window_typeLAYOUT = (OfInt)LAYOUT.select(groupElement("receiving_beam_weighting_window_type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_type
     * }
     */
    public static final OfInt receiving_beam_weighting_window_typeLAYOUT() {
        return receiving_beam_weighting_window_typeLAYOUT;
    }

    private static final long receiving_beam_weighting_window_type$OFFSET = 200;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_type
     * }
     */
    public static final long receiving_beam_weighting_window_type$offset() {
        return receiving_beam_weighting_window_type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_type
     * }
     */
    public static int receiving_beam_weighting_window_type(MemorySegment struct) {
        return struct.get(receiving_beam_weighting_window_typeLAYOUT, receiving_beam_weighting_window_type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_type
     * }
     */
    public static void receiving_beam_weighting_window_type(MemorySegment struct, int fieldValue) {
        struct.set(receiving_beam_weighting_window_typeLAYOUT, receiving_beam_weighting_window_type$OFFSET, fieldValue);
    }

    private static final OfInt receiving_beam_weighting_window_paramLAYOUT = (OfInt)LAYOUT.select(groupElement("receiving_beam_weighting_window_param"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_param
     * }
     */
    public static final OfInt receiving_beam_weighting_window_paramLAYOUT() {
        return receiving_beam_weighting_window_paramLAYOUT;
    }

    private static final long receiving_beam_weighting_window_param$OFFSET = 204;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_param
     * }
     */
    public static final long receiving_beam_weighting_window_param$offset() {
        return receiving_beam_weighting_window_param$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_param
     * }
     */
    public static int receiving_beam_weighting_window_param(MemorySegment struct) {
        return struct.get(receiving_beam_weighting_window_paramLAYOUT, receiving_beam_weighting_window_param$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int receiving_beam_weighting_window_param
     * }
     */
    public static void receiving_beam_weighting_window_param(MemorySegment struct, int fieldValue) {
        struct.set(receiving_beam_weighting_window_paramLAYOUT, receiving_beam_weighting_window_param$OFFSET, fieldValue);
    }

    private static final OfInt receive_flagsLAYOUT = (OfInt)LAYOUT.select(groupElement("receive_flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int receive_flags
     * }
     */
    public static final OfInt receive_flagsLAYOUT() {
        return receive_flagsLAYOUT;
    }

    private static final long receive_flags$OFFSET = 208;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int receive_flags
     * }
     */
    public static final long receive_flags$offset() {
        return receive_flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int receive_flags
     * }
     */
    public static int receive_flags(MemorySegment struct) {
        return struct.get(receive_flagsLAYOUT, receive_flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int receive_flags
     * }
     */
    public static void receive_flags(MemorySegment struct, int fieldValue) {
        struct.set(receive_flagsLAYOUT, receive_flags$OFFSET, fieldValue);
    }

    private static final OfDouble receive_beam_widthLAYOUT = (OfDouble)LAYOUT.select(groupElement("receive_beam_width"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double receive_beam_width
     * }
     */
    public static final OfDouble receive_beam_widthLAYOUT() {
        return receive_beam_widthLAYOUT;
    }

    private static final long receive_beam_width$OFFSET = 216;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double receive_beam_width
     * }
     */
    public static final long receive_beam_width$offset() {
        return receive_beam_width$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double receive_beam_width
     * }
     */
    public static double receive_beam_width(MemorySegment struct) {
        return struct.get(receive_beam_widthLAYOUT, receive_beam_width$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double receive_beam_width
     * }
     */
    public static void receive_beam_width(MemorySegment struct, double fieldValue) {
        struct.set(receive_beam_widthLAYOUT, receive_beam_width$OFFSET, fieldValue);
    }

    private static final OfDouble range_filt_minLAYOUT = (OfDouble)LAYOUT.select(groupElement("range_filt_min"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double range_filt_min
     * }
     */
    public static final OfDouble range_filt_minLAYOUT() {
        return range_filt_minLAYOUT;
    }

    private static final long range_filt_min$OFFSET = 224;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double range_filt_min
     * }
     */
    public static final long range_filt_min$offset() {
        return range_filt_min$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double range_filt_min
     * }
     */
    public static double range_filt_min(MemorySegment struct) {
        return struct.get(range_filt_minLAYOUT, range_filt_min$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double range_filt_min
     * }
     */
    public static void range_filt_min(MemorySegment struct, double fieldValue) {
        struct.set(range_filt_minLAYOUT, range_filt_min$OFFSET, fieldValue);
    }

    private static final OfDouble range_filt_maxLAYOUT = (OfDouble)LAYOUT.select(groupElement("range_filt_max"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double range_filt_max
     * }
     */
    public static final OfDouble range_filt_maxLAYOUT() {
        return range_filt_maxLAYOUT;
    }

    private static final long range_filt_max$OFFSET = 232;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double range_filt_max
     * }
     */
    public static final long range_filt_max$offset() {
        return range_filt_max$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double range_filt_max
     * }
     */
    public static double range_filt_max(MemorySegment struct) {
        return struct.get(range_filt_maxLAYOUT, range_filt_max$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double range_filt_max
     * }
     */
    public static void range_filt_max(MemorySegment struct, double fieldValue) {
        struct.set(range_filt_maxLAYOUT, range_filt_max$OFFSET, fieldValue);
    }

    private static final OfDouble depth_filt_minLAYOUT = (OfDouble)LAYOUT.select(groupElement("depth_filt_min"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double depth_filt_min
     * }
     */
    public static final OfDouble depth_filt_minLAYOUT() {
        return depth_filt_minLAYOUT;
    }

    private static final long depth_filt_min$OFFSET = 240;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double depth_filt_min
     * }
     */
    public static final long depth_filt_min$offset() {
        return depth_filt_min$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double depth_filt_min
     * }
     */
    public static double depth_filt_min(MemorySegment struct) {
        return struct.get(depth_filt_minLAYOUT, depth_filt_min$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double depth_filt_min
     * }
     */
    public static void depth_filt_min(MemorySegment struct, double fieldValue) {
        struct.set(depth_filt_minLAYOUT, depth_filt_min$OFFSET, fieldValue);
    }

    private static final OfDouble depth_filt_maxLAYOUT = (OfDouble)LAYOUT.select(groupElement("depth_filt_max"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double depth_filt_max
     * }
     */
    public static final OfDouble depth_filt_maxLAYOUT() {
        return depth_filt_maxLAYOUT;
    }

    private static final long depth_filt_max$OFFSET = 248;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double depth_filt_max
     * }
     */
    public static final long depth_filt_max$offset() {
        return depth_filt_max$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double depth_filt_max
     * }
     */
    public static double depth_filt_max(MemorySegment struct) {
        return struct.get(depth_filt_maxLAYOUT, depth_filt_max$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double depth_filt_max
     * }
     */
    public static void depth_filt_max(MemorySegment struct, double fieldValue) {
        struct.set(depth_filt_maxLAYOUT, depth_filt_max$OFFSET, fieldValue);
    }

    private static final OfDouble absorptionLAYOUT = (OfDouble)LAYOUT.select(groupElement("absorption"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double absorption
     * }
     */
    public static final OfDouble absorptionLAYOUT() {
        return absorptionLAYOUT;
    }

    private static final long absorption$OFFSET = 256;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double absorption
     * }
     */
    public static final long absorption$offset() {
        return absorption$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double absorption
     * }
     */
    public static double absorption(MemorySegment struct) {
        return struct.get(absorptionLAYOUT, absorption$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double absorption
     * }
     */
    public static void absorption(MemorySegment struct, double fieldValue) {
        struct.set(absorptionLAYOUT, absorption$OFFSET, fieldValue);
    }

    private static final OfDouble sound_velocityLAYOUT = (OfDouble)LAYOUT.select(groupElement("sound_velocity"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double sound_velocity
     * }
     */
    public static final OfDouble sound_velocityLAYOUT() {
        return sound_velocityLAYOUT;
    }

    private static final long sound_velocity$OFFSET = 264;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double sound_velocity
     * }
     */
    public static final long sound_velocity$offset() {
        return sound_velocity$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double sound_velocity
     * }
     */
    public static double sound_velocity(MemorySegment struct) {
        return struct.get(sound_velocityLAYOUT, sound_velocity$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double sound_velocity
     * }
     */
    public static void sound_velocity(MemorySegment struct, double fieldValue) {
        struct.set(sound_velocityLAYOUT, sound_velocity$OFFSET, fieldValue);
    }

    private static final OfDouble spreadingLAYOUT = (OfDouble)LAYOUT.select(groupElement("spreading"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double spreading
     * }
     */
    public static final OfDouble spreadingLAYOUT() {
        return spreadingLAYOUT;
    }

    private static final long spreading$OFFSET = 272;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double spreading
     * }
     */
    public static final long spreading$offset() {
        return spreading$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double spreading
     * }
     */
    public static double spreading(MemorySegment struct) {
        return struct.get(spreadingLAYOUT, spreading$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double spreading
     * }
     */
    public static void spreading(MemorySegment struct, double fieldValue) {
        struct.set(spreadingLAYOUT, spreading$OFFSET, fieldValue);
    }

    private static final OfByte raw_data_from_7027LAYOUT = (OfByte)LAYOUT.select(groupElement("raw_data_from_7027"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char raw_data_from_7027
     * }
     */
    public static final OfByte raw_data_from_7027LAYOUT() {
        return raw_data_from_7027LAYOUT;
    }

    private static final long raw_data_from_7027$OFFSET = 280;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char raw_data_from_7027
     * }
     */
    public static final long raw_data_from_7027$offset() {
        return raw_data_from_7027$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char raw_data_from_7027
     * }
     */
    public static byte raw_data_from_7027(MemorySegment struct) {
        return struct.get(raw_data_from_7027LAYOUT, raw_data_from_7027$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char raw_data_from_7027
     * }
     */
    public static void raw_data_from_7027(MemorySegment struct, byte fieldValue) {
        struct.set(raw_data_from_7027LAYOUT, raw_data_from_7027$OFFSET, fieldValue);
    }

    private static final SequenceLayout reserved_2LAYOUT = (SequenceLayout)LAYOUT.select(groupElement("reserved_2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static final SequenceLayout reserved_2LAYOUT() {
        return reserved_2LAYOUT;
    }

    private static final long reserved_2$OFFSET = 281;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static final long reserved_2$offset() {
        return reserved_2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static MemorySegment reserved_2(MemorySegment struct) {
        return struct.asSlice(reserved_2$OFFSET, reserved_2LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static void reserved_2(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, reserved_2$OFFSET, reserved_2LAYOUT.byteSize());
    }

    private static long[] reserved_2$DIMS = { 15 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static long[] reserved_2$dimensions() {
        return reserved_2$DIMS;
    }
    private static final VarHandle reserved_2$ELEM_HANDLE = reserved_2LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static byte reserved_2(MemorySegment struct, long index0) {
        return (byte)reserved_2$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * char reserved_2[15]
     * }
     */
    public static void reserved_2(MemorySegment struct, long index0, byte fieldValue) {
        reserved_2$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final OfByte sv_sourceLAYOUT = (OfByte)LAYOUT.select(groupElement("sv_source"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char sv_source
     * }
     */
    public static final OfByte sv_sourceLAYOUT() {
        return sv_sourceLAYOUT;
    }

    private static final long sv_source$OFFSET = 296;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char sv_source
     * }
     */
    public static final long sv_source$offset() {
        return sv_source$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char sv_source
     * }
     */
    public static byte sv_source(MemorySegment struct) {
        return struct.get(sv_sourceLAYOUT, sv_source$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char sv_source
     * }
     */
    public static void sv_source(MemorySegment struct, byte fieldValue) {
        struct.set(sv_sourceLAYOUT, sv_source$OFFSET, fieldValue);
    }

    private static final OfByte layer_comp_flagLAYOUT = (OfByte)LAYOUT.select(groupElement("layer_comp_flag"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char layer_comp_flag
     * }
     */
    public static final OfByte layer_comp_flagLAYOUT() {
        return layer_comp_flagLAYOUT;
    }

    private static final long layer_comp_flag$OFFSET = 297;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char layer_comp_flag
     * }
     */
    public static final long layer_comp_flag$offset() {
        return layer_comp_flag$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char layer_comp_flag
     * }
     */
    public static byte layer_comp_flag(MemorySegment struct) {
        return struct.get(layer_comp_flagLAYOUT, layer_comp_flag$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char layer_comp_flag
     * }
     */
    public static void layer_comp_flag(MemorySegment struct, byte fieldValue) {
        struct.set(layer_comp_flagLAYOUT, layer_comp_flag$OFFSET, fieldValue);
    }

    private static final SequenceLayout reserved_3LAYOUT = (SequenceLayout)LAYOUT.select(groupElement("reserved_3"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static final SequenceLayout reserved_3LAYOUT() {
        return reserved_3LAYOUT;
    }

    private static final long reserved_3$OFFSET = 298;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static final long reserved_3$offset() {
        return reserved_3$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static MemorySegment reserved_3(MemorySegment struct) {
        return struct.asSlice(reserved_3$OFFSET, reserved_3LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static void reserved_3(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, reserved_3$OFFSET, reserved_3LAYOUT.byteSize());
    }

    private static long[] reserved_3$DIMS = { 8 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static long[] reserved_3$dimensions() {
        return reserved_3$DIMS;
    }
    private static final VarHandle reserved_3$ELEM_HANDLE = reserved_3LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static byte reserved_3(MemorySegment struct, long index0) {
        return (byte)reserved_3$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * char reserved_3[8]
     * }
     */
    public static void reserved_3(MemorySegment struct, long index0, byte fieldValue) {
        reserved_3$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

}

