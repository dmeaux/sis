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
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.PlanarProjection;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.operation.projection.NormalizedProjection;


/**
 * The provider for <cite>"Modified Azimuthal Equidistant"</cite> projection (EPSG:9832).
 *
 * <h2>Relationship with "Azimuthal Equidistant"</h2>
 * The <cite>Modified Azimuthal Equidistant</cite> projection is an approximation of a theoretically
 * more generic oblique Azimuthal Equidistant projection. But Snyder's <u>Map Projection — a working
 * manual</u> book actually gives formulas for the same scope than the one given by EPSG, namely for
 * islands in Micronesia. Consequently, we assume that what is commonly presented by other libraries
 * as a "generic" Azimuthal Equidistant projection is actually the Modified Azimuthal Equidistant
 * approximation.
 *
 * @author  Martin Desruisseaux (Geomatys)
 *
 * @see <a href="http://geotiff.maptools.org/proj_list/azimuthal_equidistant.html">GeoTIFF parameters for Azimuthal Equidistant</a>
 */
@XmlTransient
public final class ModifiedAzimuthalEquidistant extends MapProjection {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -9025540136016917410L;

    /**
     * The operation parameter descriptor for the <cite>Latitude of natural origin</cite> (φ₀) parameter value.
     * Valid values range is (-90 … 90)° and default value is 0°.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> Latitude of natural origin </td></tr>
     *   <tr><td> OGC:     </td><td> latitude_of_origin </td></tr>
     *   <tr><td> GeoTIFF: </td><td> CenterLat </td></tr>
     *   <tr><td> Proj4:   </td><td> lat_0 </td></tr>
     * </table>
     */
    public static final ParameterDescriptor<Double> LATITUDE_OF_ORIGIN;

    /**
     * The operation parameter descriptor for the <cite>Longitude of natural origin</cite> (λ₀) parameter value.
     * Valid values range is [-180 … 180]° and default value is 0°.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> Longitude of natural origin </td></tr>
     *   <tr><td> OGC:     </td><td> central_meridian </td></tr>
     *   <tr><td> GeoTIFF: </td><td> CenterLong </td></tr>
     *   <tr><td> Proj4:   </td><td> lon_0 </td></tr>
     * </table>
     */
    public static final ParameterDescriptor<Double> LONGITUDE_OF_ORIGIN;

    /**
     * The operation parameter descriptor for the <cite>False easting</cite> (FE) parameter value.
     * Valid values range is unrestricted and default value is 0 metre.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> False easting </td></tr>
     *   <tr><td> OGC:     </td><td> false_easting </td></tr>
     *   <tr><td> GeoTIFF: </td><td> FalseEasting </td></tr>
     *   <tr><td> Proj4:   </td><td> x_0 </td></tr>
     * </table>
     */
    public static final ParameterDescriptor<Double> FALSE_EASTING;

    /**
     * The operation parameter descriptor for the <cite>False northing</cite> (FN) parameter value.
     * Valid values range is unrestricted and default value is 0 metre.
     *
     * <!-- Generated by ParameterNameTableGenerator -->
     * <table class="sis">
     *   <caption>Parameter names</caption>
     *   <tr><td> EPSG:    </td><td> False northing </td></tr>
     *   <tr><td> OGC:     </td><td> false_northing </td></tr>
     *   <tr><td> GeoTIFF: </td><td> FalseNorthing </td></tr>
     *   <tr><td> Proj4:   </td><td> y_0 </td></tr>
     * </table>
     */
    public static final ParameterDescriptor<Double> FALSE_NORTHING;

    /**
     * Returns a parameter with the same names and identifiers than the given parameter,
     * except (OGC), ESRI and netCDF names which are omitted. We omit those names for now
     * because we have not seen a reference about what those parameter names should be.
     * The OGC names are kept despite that because it uses the same names for most projection.
     * This may be revisited in future SIS versions.
     *
     * <p>The OGC and GeoTIFF names kept by this method are actually the names for
     * <cite>Azimuthal Equidistant</cite> (not modified) projection.</p>
     */
    private static ParameterBuilder erase(final ParameterBuilder builder, ParameterDescriptor<?> template) {
        return builder.addNamesAndIdentifiers(template)                         // Copy from this parameter…
                      .rename(Citations.ESRI,   (CharSequence[]) null)          // … except for those names.
                      .rename(Citations.NETCDF, (CharSequence[]) null);
    }

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    private static final ParameterDescriptorGroup PARAMETERS;
    static {
        final ParameterBuilder builder = builder();
        LATITUDE_OF_ORIGIN  = createLatitude (erase(builder, Orthographic.LATITUDE_OF_ORIGIN), true);
        LONGITUDE_OF_ORIGIN = createLongitude(erase(builder, Orthographic.LONGITUDE_OF_ORIGIN));
        FALSE_EASTING       = createShift    (erase(builder, Orthographic.FALSE_EASTING));
        FALSE_NORTHING      = createShift    (erase(builder, Orthographic.FALSE_NORTHING));

        PARAMETERS = builder.addIdentifier("9832")
                .addName("Modified Azimuthal Equidistant")
                .addName(Citations.GEOTIFF,  "CT_AzimuthalEquidistant")     // See discussion in class javadoc.
                .addName(Citations.PROJ4,    "aeqd")
                .addIdentifier(Citations.GEOTIFF, "12")
                .createGroupForMapProjection(
                        LATITUDE_OF_ORIGIN,
                        LONGITUDE_OF_ORIGIN,
                        FALSE_EASTING,
                        FALSE_NORTHING);
    }

    /**
     * Constructs a new provider.
     */
    public ModifiedAzimuthalEquidistant() {
        super(PlanarProjection.class, PARAMETERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return the map projection created from the given parameter values.
     */
    @Override
    protected final NormalizedProjection createProjection(final Parameters parameters) {
        return new org.apache.sis.referencing.operation.projection.ModifiedAzimuthalEquidistant(this, parameters);
    }
}
