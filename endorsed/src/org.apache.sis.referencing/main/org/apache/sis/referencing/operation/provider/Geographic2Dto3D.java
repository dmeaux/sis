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
package org.apache.sis.referencing.operation.provider;

import jakarta.xml.bind.annotation.XmlTransient;
import org.opengis.util.FactoryException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.util.internal.Constants;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.parameter.Parameters;


/**
 * The provider for <q>Geographic 2D to 3D conversion</q>.
 * The default operation sets the ellipsoidal height to zero.
 *
 * <p>This operation is a SIS extension; the EPSG dataset defines only the 3D to 2D case.
 * Consequently, WKT formatting will not represent "2D to 3D" transform. Instead, WKT will
 * format the inverse ({@code "INVERSE_MT"}) of 3D to 2D transform.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 *
 * @see Geographic3Dto2D
 */
@XmlTransient
public final class Geographic2Dto3D extends GeographicRedimension {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -1198461394243672064L;

    /**
     * The ellipsoidal height to set.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> SIS:     </td><td> height </td></tr>
     * </table>
     */
    public static final ParameterDescriptor<Double> HEIGHT;

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    public static final ParameterDescriptorGroup PARAMETERS;
    static {
        final ParameterBuilder builder = builder().setCodeSpace(Citations.SIS, Constants.SIS);
        HEIGHT = createShift(builder.addName("height"));
        PARAMETERS = builder.addName("Geographic2D to 3D conversion").createGroup(HEIGHT);
    }

    /**
     * Constructs a provider that can be resized.
     */
    Geographic2Dto3D(final int indexOfDim) {
        super(PARAMETERS, indexOfDim);
    }

    /**
     * Constructs a provider with default parameters.
     *
     * @deprecated This is a temporary constructor before replacement by a {@code provider()} method with JDK9.
     */
    @Deprecated
    public Geographic2Dto3D() {
        super(Geographic3Dto2D.REDIMENSIONED[1]);
    }

    /**
     * Returns the inverse of this operation.
     */
    @Override
    public AbstractProvider inverse() {
        return Geographic3Dto2D.REDIMENSIONED[2];
    }

    /**
     * Returns the transform.
     *
     * @param  factory  the factory for creating affine transforms.
     * @param  values   the parameter values.
     * @return the math transform for the given parameter values.
     * @throws FactoryException if an error occurred while creating the transform.
     */
    @Override
    public MathTransform createMathTransform(MathTransformFactory factory, ParameterValueGroup values)
            throws FactoryException
    {
        final Parameters pv = Parameters.castOrWrap(values);
        final MatrixSIS m = Matrices.createDiagonal(4, 3);
        m.setElement(2, 2, pv.doubleValue(HEIGHT));
        m.setElement(3, 2, 1);
        return factory.createAffineTransform(m);
    }
}
