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
package org.apache.sis.internal.referencing.provider;

import javax.xml.bind.annotation.XmlTransient;
import org.opengis.util.FactoryException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.apache.sis.referencing.operation.transform.PoleRotation;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.measure.Longitude;
import org.apache.sis.measure.Latitude;
import org.apache.sis.measure.Units;


/**
 * The provider for the NetCDF <cite>Rotated Latitude/Longitude</cite> coordinate operation.
 * This is similar to the WMO Rotated Latitude/Longitude but rotating north pole instead of south pole.
 * The 0° rotated meridian is defined as the meridian that runs through both the geographical and the rotated North pole.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.3
 *
 * @see SouthPoleRotation
 * @see <a href="https://cfconventions.org/cf-conventions/cf-conventions.html#_rotated_pole">Rotated pole in CF-conventions</a>
 *
 * @since 1.2
 * @module
 */
@XmlTransient
public final class NorthPoleRotation extends AbstractProvider {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 3485083285768740448L;

    /**
     * The operation parameter descriptor for the <cite>grid north pole latitude</cite> parameter value.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> SIS:     </td><td> Latitude of rotated pole </td></tr>
     *   <tr><td> NetCDF:  </td><td> grid_north_pole_latitude </td></tr>
     * </table>
     * <b>Notes:</b>
     * <ul>
     *   <li>No default value</li>
     * </ul>
     */
    private static final ParameterDescriptor<Double> POLE_LATITUDE;

    /**
     * The operation parameter descriptor for the <cite>grid north pole longitude</cite> parameter value.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> SIS:     </td><td> Longitude of rotated pole </td></tr>
     *   <tr><td> NetCDF:  </td><td> grid_north_pole_longitude </td></tr>
     * </table>
     * <b>Notes:</b>
     * <ul>
     *   <li>No default value</li>
     * </ul>
     */
    private static final ParameterDescriptor<Double> POLE_LONGITUDE;

    /**
     * The operation parameter descriptor for the <cite>north_pole_grid_longitude</cite> parameter value.
     * This parameter is optional.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> SIS:     </td><td> Axis rotation </td></tr>
     *   <tr><td> NetCDF:  </td><td> north_pole_grid_longitude </td></tr>
     * </table>
     * <b>Notes:</b>
     * <ul>
     *   <li>Value domain: [-180.0 … 180.0]°</li>
     *   <li>Optional</li>
     * </ul>
     */
    private static final ParameterDescriptor<Double> AXIS_ANGLE;

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    public static final ParameterDescriptorGroup PARAMETERS;
    static {
        final ParameterBuilder builder = new ParameterBuilder().setCodeSpace(Citations.NETCDF, "NetCDF").setRequired(true);

        POLE_LATITUDE = builder
                .addNameAndIdentifier(Citations.SIS, SouthPoleRotation.POLE_LATITUDE)
                .addName("grid_north_pole_latitude")
                .createBounded(Latitude.MIN_VALUE, Latitude.MAX_VALUE, Double.NaN, Units.DEGREE);

        POLE_LONGITUDE = builder
                .addNameAndIdentifier(Citations.SIS, SouthPoleRotation.POLE_LONGITUDE)
                .addName("grid_north_pole_longitude")
                .createBounded(Longitude.MIN_VALUE, Longitude.MAX_VALUE, Double.NaN, Units.DEGREE);

        AXIS_ANGLE = builder.setRequired(false)
                .addNameAndIdentifier(Citations.SIS, SouthPoleRotation.AXIS_ANGLE)
                .addName("north_pole_grid_longitude")
                .createBounded(Longitude.MIN_VALUE, Longitude.MAX_VALUE, 0, Units.DEGREE);

        PARAMETERS = builder.setRequired(true)
                .addName(Citations.SIS, "North pole rotation")
                .addName("rotated_latitude_longitude")
                .createGroup(POLE_LATITUDE,    // Note: `PoleRotation` implementation depends on this parameter order.
                             POLE_LONGITUDE,
                             AXIS_ANGLE);
    }

    /**
     * Constructs a new provider.
     */
    public NorthPoleRotation() {
        super(Conversion.class, PARAMETERS,
              EllipsoidalCS.class, 2, false,
              EllipsoidalCS.class, 2, false);
    }

    /**
     * Creates a coordinate operation from the specified group of parameter values.
     *
     * @param  factory     the factory to use for creating the transforms.
     * @param  parameters  the group of parameter values.
     * @return the coordinate operation created from the given parameter values.
     * @throws FactoryException if the coordinate operation can not be created.
     */
    @Override
    public MathTransform createMathTransform(final MathTransformFactory factory, final ParameterValueGroup parameters)
            throws FactoryException
    {
        final Parameters p = Parameters.castOrWrap(parameters);
        return PoleRotation.rotateNorthPole(factory,
                p.getValue(POLE_LATITUDE),
                p.getValue(POLE_LONGITUDE),
                p.getValue(AXIS_ANGLE));
    }
}
