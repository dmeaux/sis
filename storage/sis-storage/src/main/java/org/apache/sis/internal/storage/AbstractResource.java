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
package org.apache.sis.internal.storage;

import java.util.Optional;
import org.opengis.util.GenericName;
import org.opengis.metadata.Metadata;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.referencing.CRS;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.apache.sis.storage.event.StoreListeners;
import org.apache.sis.storage.event.WarningEvent;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.NilReason;


/**
 * Base implementation of resources contained in data stores. This class provides a {@link #getMetadata()}
 * method which extracts information from other methods. Subclasses should override the following methods:
 *
 * <ul>
 *   <li>{@link #getIdentifier()} (strongly recommended)</li>
 *   <li>{@link #getEnvelope()} (recommended)</li>
 *   <li>{@link #createMetadata()} (optional)</li>
 *   <li>{@link #getSynchronizationLock()} (optional)</li>
 * </ul>
 *
 * This class extends {@link StoreListeners} for convenience reasons.
 * This implementation details may change in any future SIS version.
 *
 * <h2>Thread safety</h2>
 * Default methods of this abstract class are thread-safe.
 * Synchronization, when needed, uses {@link #getSynchronizationLock()}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 * @since   0.8
 * @module
 */
public class AbstractResource extends StoreListeners implements Resource {
    /**
     * A description of this resource as an unmodifiable metadata, or {@code null} if not yet computed.
     * If non-null, this metadata should contain at least the resource {@linkplain #getIdentifier() identifier}.
     * Those metadata are created by {@link #getMetadata()} when first needed.
     */
    private volatile Metadata metadata;

    /**
     * Creates a new resource. This resource will have its own set of listeners,
     * but the listeners of the data store that created this resource will be notified as well.
     *
     * @param  parent  listeners of the parent resource, or {@code null} if none.
     *         This is usually the listeners of the {@link org.apache.sis.storage.DataStore}
     *         that created this resource.
     */
    public AbstractResource(final StoreListeners parent) {
        super(parent, null);
    }

    /**
     * Returns the resource persistent identifier if available.
     * The default implementation returns an empty value.
     * Subclasses are strongly encouraged to override if they can provide a value.
     *
     * <div class="note"><b>Implementation note:</b>
     * the default implementation of {@link #createMetadata()} uses this identifier for initializing
     * the {@code metadata/identificationInfo/citation/title} property. So it is generally not useful
     * to fallback on metadata if the identifier is empty.</div>
     */
    @Override
    public Optional<GenericName> getIdentifier() throws DataStoreException {
        return Optional.empty();
    }

    /**
     * Returns the spatiotemporal envelope of this resource. This information is part of API only in some kinds of resource
     * like {@link org.apache.sis.storage.FeatureSet}. But the method is provided in this base class for convenience and for
     * allowing {@link #createMetadata()} to use this information if available. The default implementation gives an empty value.
     *
     * @return the spatiotemporal resource extent.
     * @throws DataStoreException if an error occurred while reading or computing the envelope.
     */
    public Optional<Envelope> getEnvelope() throws DataStoreException {
        return Optional.empty();
    }

    /**
     * Returns a description of this resource. This method invokes {@link #createMetadata()}
     * in a synchronized block when first needed, then caches the result.
     *
     * @return information about this resource (never {@code null} in this implementation).
     * @throws DataStoreException if an error occurred while reading or computing the metadata.
     */
    @Override
    public final Metadata getMetadata() throws DataStoreException {
        Metadata md = metadata;
        if (md == null) {
            synchronized (getSynchronizationLock()) {
                md = metadata;
                if (md == null) {
                    md = createMetadata();
                    if (md == null) {
                        md = NilReason.UNKNOWN.createNilObject(Metadata.class);
                    }
                    metadata = md;
                }
            }
        }
        return md;
    }

    /**
     * Invoked in a synchronized block the first time that {@link #getMetadata()} is invoked.
     * The default implementation populates metadata based on information
     * provided by {@link #getIdentifier()} and {@link #getEnvelope()}.
     * Subclasses should override if they can provide more information.
     *
     * @return the newly created metadata, or {@code null} if unknown.
     * @throws DataStoreException if an error occurred while reading metadata from this resource.
     */
    protected Metadata createMetadata() throws DataStoreException {
        final MetadataBuilder builder = new MetadataBuilder();
        builder.addDefaultMetadata(this, this);
        return builder.build(true);
    }

    /**
     * Clears any cache in this resource, forcing the data to be recomputed when needed again.
     * The default implementation clears the cached metadata object, which will cause
     * {@link #createMetadata()} to be invoked again when first needed.
     */
    protected void clearCache() {
        metadata = null;
    }

    /**
     * Returns the object on which to perform synchronizations for thread-safety.
     *
     * @return the synchronization lock.
     */
    protected Object getSynchronizationLock() {
        return this;
    }

    /**
     * Registers only listeners for {@link WarningEvent}s on the assumption that most resources
     * (at least the read-only ones) produce no change events.
     */
    @Override
    public <T extends StoreEvent> void addListener(Class<T> eventType, StoreListener<? super T> listener) {
        // If an argument is null, we let the parent class throws (indirectly) NullArgumentException.
        if (listener == null || eventType == null || eventType.isAssignableFrom(WarningEvent.class)) {
            super.addListener(eventType, listener);
        }
    }

    /**
     * Creates an exception message for a resource that can not be read.
     * If the error is potentially created by a request out of bounds,
     * this method tries to build a message with the problematic coordinates.
     *
     * @param  filename  some identification (typically a file name) of the data that can not be read.
     * @param  request   the requested domain, or {@code null} if the problem is not a request out of bounds.
     * @return the message to provide in an exception.
     */
    final String createExceptionMessage(final String filename, Envelope request) {
        String message = Errors.getResources(getLocale()).getString(Errors.Keys.CanNotRead_1, filename);
        if (request != null) try {
            Envelope envelope = getEnvelope().orElse(null);
            if (envelope != null) {
                final CoordinateReferenceSystem crs = CRS.suggestCommonTarget(null,
                        envelope.getCoordinateReferenceSystem(),
                        request.getCoordinateReferenceSystem());
                request  = Envelopes.transform(request,  crs);
                envelope = Envelopes.transform(envelope, crs);
                final int dimension = request.getDimension();
                StringBuilder buffer = null;
                for (int i=0; i<dimension; i++) {
                    final double rmin =  request.getMinimum(i);
                    final double rmax =  request.getMaximum(i);
                    final double vmin = envelope.getMinimum(i);
                    final double vmax = envelope.getMaximum(i);
                    if (rmax < vmin || rmin > vmax) {
                        final String axis;
                        if (crs != null) {
                            axis = IdentifiedObjects.getDisplayName(crs.getCoordinateSystem().getAxis(i), getLocale());
                        } else if (i < 3) {
                            axis = String.valueOf((char) ('x' + i));
                        } else {
                            axis = "#" + (i+1);
                        }
                        if (buffer == null) {
                            buffer = new StringBuilder(message);
                        }
                        buffer.append(System.lineSeparator()).append(" • ").append(Resources.forLocale(getLocale())
                                .getString(Resources.Keys.RequestOutOfBounds_5, axis, vmin, vmax, rmin, rmax));
                    }
                }
                if (buffer != null) {
                    message = buffer.toString();
                }
            }
        } catch (DataStoreException | TransformException e) {
            Logging.ignorableException(getLogger(), AbstractResource.class, "createExceptionMessage", e);
        }
        return message;
    }
}
