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

import java.util.Arrays;
import jakarta.xml.bind.annotation.XmlTransient;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.Transformation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.apache.sis.measure.Units;


/**
 * Base class for all transformations that perform a translation in the geographic domain.
 * This base class defines a provider for <q>Geographic3D offsets</q> (EPSG:9660),
 * but subclasses will provide different operations.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
@XmlTransient
public class GeographicOffsets extends GeodeticOperation {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -6246011184175753328L;

    /**
     * The operation parameter descriptor for the <q>Longitude offset</q> parameter value.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> Longitude offset </td></tr>
     * </table>
     */
    static final ParameterDescriptor<Double> TX;

    /**
     * The operation parameter descriptor for the <q>Latitude offset</q> parameter value.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> Latitude offset </td></tr>
     * </table>
     */
    static final ParameterDescriptor<Double> TY;

    /**
     * The operation parameter descriptor for the <q>Vertical Offset</q> parameter value.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> Vertical Offset </td></tr>
     * </table>
     */
    static final ParameterDescriptor<Double> TZ;

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    private static final ParameterDescriptorGroup PARAMETERS;
    static {
        final ParameterBuilder builder = builder();
        TY = builder.addIdentifier("8601").addName("Latitude offset") .create(0, Units.DEGREE);
        TX = builder.addIdentifier("8602").addName("Longitude offset").create(0, Units.DEGREE);
        TZ = builder.addIdentifier("8603").addName("Vertical Offset") .create(0, Units.METRE);
        PARAMETERS = builder.addIdentifier("9660").addName("Geographic3D offsets").createGroup(TY, TX, TZ);
    }

    /**
     * The providers for all combinations between 2D and 3D cases.
     */
    private static final GeographicOffsets[] REDIMENSIONED = new GeographicOffsets[4];
    static {
        Arrays.setAll(REDIMENSIONED, (i) -> (i == INDEX_OF_2D)
                ? new GeographicOffsets2D(i)
                : new GeographicOffsets(i));
    }

    /**
     * Returns the provider for the specified combination of source and target dimensions.
     */
    @Override
    GeodeticOperation redimensioned(int indexOfDim) {
        return REDIMENSIONED[indexOfDim];
    }

    /**
     * Returns the two-dimensional case of this provider.
     */
    static GeographicOffsets provider2D() {
        return REDIMENSIONED[INDEX_OF_2D];
    }

    /**
     * Creates a copy of this provider.
     *
     * @deprecated This is a temporary constructor before replacement by a {@code provider()} method with JDK9.
     */
    @Deprecated
    public GeographicOffsets() {
        super(REDIMENSIONED[INDEX_OF_3D]);
    }

    /**
     * Creates a copy of this provider.
     *
     * @deprecated This is a temporary constructor before replacement by a {@code provider()} method with JDK9.
     */
    @Deprecated
    GeographicOffsets(final GeographicOffsets copy) {
        super(copy);
    }

    /**
     * Creates a provider with the parameters of this base class.
     */
    private GeographicOffsets(int indexOfDim) {
        this(PARAMETERS, indexOfDim);
    }

    /**
     * For default constructors in this class and subclasses.
     */
    GeographicOffsets(ParameterDescriptorGroup parameters, int indexOfDim) {
        super(Transformation.class, parameters, indexOfDim,
              EllipsoidalCS.class, false,
              EllipsoidalCS.class, false);
    }

    /**
     * Returns the parameter descriptor for the vertical axis.
     */
    ParameterDescriptor<Double> vertical() {
        return TZ;
    }

    /**
     * Creates a transform from the specified group of parameter values.
     * The parameter values are unconditionally converted to degrees and metres.
     *
     * @param  factory  ignored (can be null).
     * @param  values   the group of parameter values.
     * @return the created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    @Override
    public MathTransform createMathTransform(MathTransformFactory factory, ParameterValueGroup values)
            throws ParameterNotFoundException
    {
        final Parameters pv = Parameters.castOrWrap(values);
        return MathTransforms.translation(pv.doubleValue(TX),
                                          pv.doubleValue(TY),
                                          pv.doubleValue(vertical()));

    }
}
