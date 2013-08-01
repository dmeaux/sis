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
package org.apache.sis.metadata.iso.quality;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.quality.TemporalConsistency;


/**
 * Correctness of ordered events or sequences, if reported.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "DQ_TemporalConsistency_Type")
@XmlRootElement(name = "DQ_TemporalConsistency")
public class DefaultTemporalConsistency extends AbstractTemporalAccuracy implements TemporalConsistency {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 5468604313098465903L;

    /**
     * Constructs an initially empty temporal consistency.
     */
    public DefaultTemporalConsistency() {
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object The metadata to copy values from, or {@code null} if none.
     *
     * @see #castOrCopy(TemporalConsistency)
     */
    public DefaultTemporalConsistency(final TemporalConsistency object) {
        super(object);
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable actions in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultTemporalConsistency}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultTemporalConsistency} instance is created using the
     *       {@linkplain #DefaultTemporalConsistency(TemporalConsistency) copy constructor}
     *       and returned. Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultTemporalConsistency castOrCopy(final TemporalConsistency object) {
        if (object == null || object instanceof DefaultTemporalConsistency) {
            return (DefaultTemporalConsistency) object;
        }
        return new DefaultTemporalConsistency(object);
    }
}