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
package org.apache.sis.referencing.datum;

import java.util.Map;
import javax.measure.unit.Unit;
import javax.measure.unit.NonSI;
import javax.measure.quantity.Angle;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.datum.PrimeMeridian;
import org.apache.sis.referencing.AbstractIdentifiedObject;
import org.apache.sis.internal.jaxb.gco.Measure;
import org.apache.sis.internal.util.Numerics;
import org.apache.sis.io.wkt.Formatter;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.Immutable;

import static org.apache.sis.util.ArgumentChecks.ensureFinite;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

// Related to JDK7
import org.apache.sis.internal.jdk7.Objects;


/**
 * Defines the origin from which longitude values are determined.
 *
 * {@section Creating new prime meridian instances}
 * New instances can be created either directly by specifying all information to a factory method (choices 3
 * and 4 below), or indirectly by specifying the identifier of an entry in a database (choices 1 and 2 below).
 * In particular, the <a href="http://www.epsg.org">EPSG</a> database provides definitions for many prime meridians,
 * and Apache SIS provides convenience shortcuts for some of them.
 *
 * <p>Choice 1 in the following list is the easiest but most restrictive way to get a prime meridian.
 * The other choices provide more freedom. Each choice delegates its work to the subsequent items
 * (in the default configuration), so this list can been seen as <cite>top to bottom</cite> API.</p>
 *
 * <ol>
 *   <li>Create a {@code PrimeMeridian} from one of the static convenience shortcuts listed in
 *       {@link org.apache.sis.referencing.GeodeticObjects#primeMeridian()}.</li>
 *   <li>Create a {@code PrimeMeridian} from an identifier in a database by invoking
 *       {@link org.opengis.referencing.datum.DatumAuthorityFactory#createPrimeMeridian(String)}.</li>
 *   <li>Create a {@code PrimeMeridian} by invoking the {@code createPrimeMeridian(…)}
 *       method defined in the {@link org.opengis.referencing.datum.DatumFactory} interface.</li>
 *   <li>Create a {@code DefaultPrimeMeridian} by invoking the
 *       {@linkplain #DefaultPrimeMeridian(Map, double, Unit) constructor}.</li>
 * </ol>
 *
 * <b>Example:</b> the following code gets the Greenwich prime meridian:
 *
 * {@preformat java
 *     PrimeMeridian pm = GeodeticObjects.WGS84.primeMeridian();
 * }
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.4 (derived from geotk-1.2)
 * @version 0.4
 * @module
 *
 * @see org.apache.sis.referencing.GeodeticObjects#primeMeridian()
 */
@Immutable
@XmlType(name = "PrimeMeridianType")
@XmlRootElement(name = "PrimeMeridian")
public class DefaultPrimeMeridian extends AbstractIdentifiedObject implements PrimeMeridian {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 541978454643213305L;;

    /**
     * Longitude of the prime meridian measured from the Greenwich meridian, positive eastward.
     *
     * <p>Consider this field as final. It is declared non-final only for JAXB unmarshalling.</p>
     */
    private double greenwichLongitude;

    /**
     * The angular unit of the {@linkplain #getGreenwichLongitude() Greenwich longitude}.
     *
     * <p>Consider this field as final. It is declared non-final only for JAXB unmarshalling.</p>
     */
    private Unit<Angle> angularUnit;

    /**
     * Constructs a new object in which every attributes are set to a null value.
     * <strong>This is not a valid object.</strong> This constructor is strictly
     * reserved to JAXB, which will assign values to the fields using reflexion.
     */
    private DefaultPrimeMeridian() {
        super(org.apache.sis.internal.referencing.NilReferencingObject.INSTANCE);
    }

    /**
     * Creates a prime meridian from the given properties. The properties map is given unchanged to the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     * The following table is a reminder of main (not all) properties:
     *
     * <table class="sis">
     *   <tr>
     *     <th>Property name</th>
     *     <th>Value type</th>
     *     <th>Returned by</th>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#NAME_KEY}</td>
     *     <td>{@link ReferenceIdentifier} or {@link String}</td>
     *     <td>{@link #getName()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#ALIAS_KEY}</td>
     *     <td>{@link GenericName} or {@link CharSequence} (optionally as array)</td>
     *     <td>{@link #getAlias()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#IDENTIFIERS_KEY}</td>
     *     <td>{@link ReferenceIdentifier} (optionally as array)</td>
     *     <td>{@link #getIdentifiers()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#REMARKS_KEY}</td>
     *     <td>{@link InternationalString} or {@link String}</td>
     *     <td>{@link #getRemarks()}</td>
     *   </tr>
     * </table>
     *
     * @param properties          The properties to be given to the identified object.
     * @param greenwichLongitude  The longitude value relative to the Greenwich Meridian.
     * @param angularUnit         The angular unit of the longitude.
     */
    public DefaultPrimeMeridian(final Map<String,?> properties, final double greenwichLongitude,
                                final Unit<Angle> angularUnit)
    {
        super(properties);
        ensureFinite("greenwichLongitude", greenwichLongitude);
        ensureNonNull("angularUnit", angularUnit);
        this.greenwichLongitude = greenwichLongitude;
        this.angularUnit = angularUnit;
    }

    /**
     * Creates a new prime meridian with the same values than the specified one.
     * This copy constructor provides a way to convert an arbitrary implementation into a SIS one
     * or a user-defined one (as a subclass), usually in order to leverage some implementation-specific API.
     *
     * <p>This constructor performs a shallow copy, i.e. the properties are not cloned.</p>
     *
     * @param meridian The prime meridian to copy.
     *
     * @see #castOrCopy(PrimeMeridian)
     */
    protected DefaultPrimeMeridian(final PrimeMeridian meridian) {
        super(meridian);
        greenwichLongitude = meridian.getGreenwichLongitude();
        angularUnit        = meridian.getAngularUnit();
    }

    /**
     * Returns a SIS prime meridian implementation with the same values than the given arbitrary implementation.
     * If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a SIS implementation, then the given object is returned unchanged.
     * Otherwise a new SIS implementation is created and initialized to the attribute values of the given object.
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultPrimeMeridian castOrCopy(final PrimeMeridian object) {
        return (object == null) || (object instanceof DefaultPrimeMeridian)
                ? (DefaultPrimeMeridian) object : new DefaultPrimeMeridian(object);
    }

    /**
     * Longitude of the prime meridian measured from the Greenwich meridian, positive eastward.
     *
     * @return The prime meridian Greenwich longitude, in {@linkplain #getAngularUnit() angular unit}.
     */
    @Override
    public double getGreenwichLongitude() {
        return greenwichLongitude;
    }

    /**
     * Returns the longitude value relative to the Greenwich Meridian, expressed in the specified units.
     * This convenience method makes it easier to obtain longitude in decimal degrees using the following
     * code, regardless of the underlying angular units of this prime meridian:
     *
     * {@preformat java
     *     double longitudeInDegrees = primeMeridian.getGreenwichLongitude(NonSI.DEGREE_ANGLE);
     * }
     *
     * @param targetUnit The unit in which to express longitude.
     * @return The Greenwich longitude in the given units.
     */
    public double getGreenwichLongitude(final Unit<Angle> targetUnit) {
        return getAngularUnit().getConverterTo(targetUnit).convert(getGreenwichLongitude());
    }

    /**
     * Returns the angular unit of the Greenwich longitude.
     *
     * @return The angular unit of the {@linkplain #getGreenwichLongitude() Greenwich longitude}.
     */
    @Override
    public Unit<Angle> getAngularUnit() {
        return angularUnit;
    }

    /**
     * Invoked by JAXB for obtaining the Greenwich longitude to marshall together with its {@code "uom"} attribute.
     */
    @XmlElement(name = "greenwichLongitude", required = true)
    private Measure getGreenwichMeasure() {
        return new Measure(greenwichLongitude, angularUnit);
    }

    /**
     * Invoked by JAXB for setting the Greenwich longitude and its unit of measurement.
     */
    private void setGreenwichMeasure(final Measure measure) {
        greenwichLongitude = measure.value;
        angularUnit = measure.getUnit(Angle.class);
    }

    /**
     * Compares this prime meridian with the specified object for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  mode {@link ComparisonMode#STRICT STRICT} for performing a strict comparison, or
     *         {@link ComparisonMode#IGNORE_METADATA IGNORE_METADATA} for comparing only properties
     *         relevant to coordinate transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (!(object instanceof PrimeMeridian && super.equals(object, mode))) {
            return false;
        }
        switch (mode) {
            case STRICT: {
                final DefaultPrimeMeridian that = (DefaultPrimeMeridian) object;
                return Numerics.equals(this.greenwichLongitude, that.greenwichLongitude) &&
                        Objects.equals(this.angularUnit,        that.angularUnit);
            }
            case BY_CONTRACT: {
                final PrimeMeridian that = (PrimeMeridian) object;
                return Numerics.equals(getGreenwichLongitude(), that.getGreenwichLongitude()) &&
                        Objects.equals(getAngularUnit(),        that.getAngularUnit());
            }
            default: {
                final DefaultPrimeMeridian that = castOrCopy((PrimeMeridian) object);
                return Numerics.epsilonEqual(this.getGreenwichLongitude(NonSI.DEGREE_ANGLE),
                                             that.getGreenwichLongitude(NonSI.DEGREE_ANGLE), mode);
                /*
                 * Note: if mode==IGNORE_METADATA, we relax the unit check because EPSG uses
                 *       sexagesimal degrees for the Greenwich meridian. Requirying the same
                 *       unit prevent Geodetic.isWGS84(...) method to recognize EPSG's WGS84.
                 */
            }
        }
    }

    /**
     * Computes a hash value consistent with the given comparison mode.
     *
     * @return The hash code value for the given comparison mode.
     */
    @Override
    public int hashCode(final ComparisonMode mode) throws IllegalArgumentException {
        /*
         * The "^ (int) serialVersionUID" is an arbitrary change applied to the hash code value in order to
         * differentiate this PrimeMeridian implementation from implementations of other GeoAPI interfaces.
         */
        int code = super.hashCode(mode) ^ (int) serialVersionUID;
        switch (mode) {
            case STRICT: {
                code += Numerics.hash(greenwichLongitude, Objects.hashCode(angularUnit));
                break;
            }
            default: {
                code += Numerics.hash(getGreenwichLongitude(), Objects.hashCode(getAngularUnit()));
                break;
            }
        }
        return code;
    }

    /**
     * Formats the inner part of a <cite>Well Known Text</cite> (WKT) element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is {@code "PRIMEM"}.
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        /*
         * If the PrimeMeridian is written inside a "GEOGCS", then OGC say that it must be
         * written in the unit of the enclosing geographic coordinate system. Otherwise,
         * default to decimal degrees. Note that ESRI and GDAL don't follow this rule.
         */
        Unit<Angle> context = formatter.getConvention().getForcedUnit(Angle.class);
        if (context == null) {
            context = formatter.getAngularUnit();
            if (context == null) {
                context = NonSI.DEGREE_ANGLE;
            }
        }
        formatter.append(getGreenwichLongitude(context));
        return "PRIMEM";
    }
}