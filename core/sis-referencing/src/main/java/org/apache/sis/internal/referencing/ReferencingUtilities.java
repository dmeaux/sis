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
package org.apache.sis.internal.referencing;

import org.opengis.referencing.operation.Matrix;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.util.Static;


/**
 * A set of static methods working on GeoAPI referencing objects.
 * Some of those methods may be useful, but not really rigorous.
 * This is why they do not appear in the public packages.
 *
 * <p><strong>Do not rely on this API!</strong> It may change in incompatible way in any future release.</p>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-2.0)
 * @version 0.4
 * @module
 */
public final class ReferencingUtilities extends Static {
    /**
     * Do not allow instantiation of this class.
     */
    private ReferencingUtilities() {
    }

    /**
     * Retrieves the value at the specified row and column of the given matrix, wrapped in a {@code Number}.
     * The {@code Number} type depends on the matrix accuracy.
     *
     * @param matrix The matrix from which to get the number.
     * @param row    The row index, from 0 inclusive to {@link Matrix#getNumRow()} exclusive.
     * @param column The column index, from 0 inclusive to {@link Matrix#getNumCol()} exclusive.
     * @return       The current value at the given row and column.
     */
    public static Number getNumber(final Matrix matrix, final int row, final int column) {
        if (matrix instanceof MatrixSIS) {
            return ((MatrixSIS) matrix).getNumber(row, column);
        } else {
            return matrix.getElement(row, column);
        }
    }
}