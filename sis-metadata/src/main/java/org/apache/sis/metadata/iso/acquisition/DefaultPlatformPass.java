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
package org.apache.sis.metadata.iso.acquisition;

import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.geometry.Geometry;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.acquisition.Event;
import org.opengis.metadata.acquisition.PlatformPass;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.internal.jaxb.NonMarshalledAuthority;


/**
 * Identification of collection coverage.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.03)
 * @version 0.3
 * @module
 */
@XmlType(name = "MI_PlatformPass_Type", propOrder = {
    "identifier",
    "extent",
    "relatedEvents"
})
@XmlRootElement(name = "MI_PlatformPass")
public class DefaultPlatformPass extends ISOMetadata implements PlatformPass {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -1695097227120034432L;

    /**
     * Area covered by the pass.
     */
    private Geometry extent;

    /**
     * Occurrence of one or more events for a pass.
     */
    private Collection<Event> relatedEvents;

    /**
     * Constructs an initially empty platform pass.
     */
    public DefaultPlatformPass() {
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object The metadata to copy values from.
     *
     * @see #castOrCopy(PlatformPass)
     */
    public DefaultPlatformPass(final PlatformPass object) {
        super(object);
        identifiers   = singleton(object.getIdentifier(), Identifier.class); // TODO
        extent        = object.getExtent();
        relatedEvents = copyCollection(object.getRelatedEvents(), Event.class);
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable actions in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultPlatformPass}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultPlatformPass} instance is created using the
     *       {@linkplain #DefaultPlatformPass(PlatformPass) copy constructor}
     *       and returned. Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultPlatformPass castOrCopy(final PlatformPass object) {
        if (object == null || object instanceof DefaultPlatformPass) {
            return (DefaultPlatformPass) object;
        }
        return new DefaultPlatformPass(object);
    }

    /**
     * Returns the unique name of the pass.
     */
    @Override
    @XmlElement(name = "identifier", required = true)
    public Identifier getIdentifier() {
        return NonMarshalledAuthority.getMarshallable(identifiers);
    }

    /**
     * Sets the unique name of the pass.
     *
     * @param newValue The new identifier value.
     */
    public synchronized void setIdentifier(final Identifier newValue) {
        checkWritePermission();
        identifiers = nonNullCollection(identifiers, Identifier.class);
        NonMarshalledAuthority.setMarshallable(identifiers, newValue);
    }

    /**
     * Returns the area covered by the pass. {@code null} if unspecified.
     *
     * @todo annotate an implementation of {@link Geometry} in order to annotate this method.
     */
    @Override
    @XmlElement(name = "extent")
    public synchronized Geometry getExtent() {
        return extent;
    }

    /**
     * Sets the area covered by the pass.
     *
     * @param newValue The new extent value.
     */
    public synchronized void setExtent(final Geometry newValue) {
        checkWritePermission();
        extent = newValue;
    }

    /**
     * Returns the occurrence of one or more events for a pass.
     */
    @Override
    @XmlElement(name = "relatedEvent")
    public synchronized Collection<Event> getRelatedEvents() {
        return relatedEvents = nonNullCollection(relatedEvents, Event.class);
    }

    /**
     * Sets the occurrence of one or more events for a pass.
     *
     * @param newValues The new related events values.
     */
    public synchronized void setRelatedEvents(final Collection<? extends Event> newValues) {
        relatedEvents = writeCollection(newValues, relatedEvents, Event.class);
    }
}
