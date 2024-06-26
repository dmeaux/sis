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
package org.apache.sis.referencing.operation;

import org.opengis.referencing.operation.*;


/**
 * Implementation of {@link AbstractCoordinateOperation} methods that require knowledge about subclasses.
 * Those methods are defined in a separated static class for avoiding class loading of all implementations
 * before necessary.
 *
 * <p>This class currently provides implementation for the following methods:</p>
 * <ul>
 *   <li>{@link AbstractCoordinateOperation#castOrCopy(CoordinateOperation)}</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SubTypes {
    /**
     * Do not allow instantiation of this class.
     */
    private SubTypes() {
    }

    /**
     * Returns {@code true} if the given operation is a single operation but not a pass-through operation.
     * In an older ISO 19111 model, {@link PassThroughOperation} extended {@link SingleOperation}, which
     * was a problem for providing a value to the inherited {@link SingleOperation#getMethod()} method.
     * This has been fixed in newer ISO 19111 model, but for safety with objects following the older model
     * (e.g. GeoAPI 3.0) we are better to perform an explicit exclusion of {@link PassThroughOperation}.
     */
    static boolean isSingleOperation(final CoordinateOperation operation) {
        return (operation instanceof SingleOperation) && !(operation instanceof PassThroughOperation);
    }

    /**
     * Returns a SIS implementation for the given coordinate operation.
     *
     * @see AbstractCoordinateOperation#castOrCopy(CoordinateOperation)
     */
    static AbstractCoordinateOperation castOrCopy(final CoordinateOperation object) {
        if (object instanceof Transformation) {
            return DefaultTransformation.castOrCopy((Transformation) object);
        }
        if (object instanceof Conversion) {
            return DefaultConversion.castOrCopy((Conversion) object);
        }
        if (object instanceof PassThroughOperation) {
            return DefaultPassThroughOperation.castOrCopy((PassThroughOperation) object);
        }
        if (object instanceof ConcatenatedOperation) {
            return DefaultConcatenatedOperation.castOrCopy((ConcatenatedOperation) object);
        }
        if (isSingleOperation(object)) {
            return (object instanceof AbstractSingleOperation) ? (AbstractSingleOperation) object
                   : new AbstractSingleOperation((SingleOperation) object);
        }
        /*
         * Intentionally check for AbstractCoordinateOperation after the interfaces because user may have defined
         * his own subclass implementing the same interface.  If we were checking for AbstractCoordinateOperation
         * before the interfaces, the returned instance could have been a user subclass without the JAXB annotations
         * required for XML marshalling.
         */
        if (object == null || object instanceof AbstractCoordinateOperation) {
            return (AbstractCoordinateOperation) object;
        }
        return new AbstractCoordinateOperation(object);
    }
}
