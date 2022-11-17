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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.quality.Usability;
import org.opengis.metadata.quality.UsabilityElement;


/**
 * Degree of adherence of a dataset to a specific set of requirements.
 * The following property is mandatory in a well-formed metadata according ISO 19157:
 *
 * <div class="preformat">{@code DQ_UsabilityElement}
 * {@code   └─result……………} Value obtained from applying a data quality measure.</div>
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>Instances of this class are not synchronized for multi-threading.
 *       Synchronization, if needed, is caller's responsibility.</li>
 *   <li>Serialized objects of this class are not guaranteed to be compatible with future Apache SIS releases.
 *       Serialization support is appropriate for short term storage or RMI between applications running the
 *       same version of Apache SIS. For long term storage, use {@link org.apache.sis.xml.XML} instead.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Alexis Gaillard (Geomatys)
 * @version 1.3
 * @since   1.3
 * @module
 */
@XmlType(name = "DQ_UsabilityElement_Type")
@XmlRootElement(name = "DQ_UsabilityElement")
@XmlSeeAlso({
    DefaultUsability.class
})
public class DefaultUsabilityElement extends AbstractElement implements UsabilityElement {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 8385667875833802576L;

    /**
     * Constructs an initially empty relative internal positional accuracy.
     */
    public DefaultUsabilityElement() {
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object  the metadata to copy values from, or {@code null} if none.
     *
     * @see #castOrCopy(UsabilityElement)
     */
    public DefaultUsabilityElement(final UsabilityElement object) {
        super(object);
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable action in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultUsabilityElement}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultUsabilityElement} instance is created using the
     *       {@linkplain #DefaultUsabilityElement(UsabilityElement) copy constructor} and returned.
     *       Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object  the object to get as a SIS implementation, or {@code null} if none.
     * @return a SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    @SuppressWarnings("deprecation")
    public static DefaultUsabilityElement castOrCopy(final UsabilityElement object) {
        if (object instanceof Usability) {
            return DefaultUsability.castOrCopy((Usability) object);
        }
        if (object == null || object instanceof DefaultUsabilityElement) {
            return (DefaultUsabilityElement) object;
        }
        return new DefaultUsabilityElement(object);
    }
}
