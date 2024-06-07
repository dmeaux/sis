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
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.apache.sis.referencing.privy.WKTKeywords;
import org.apache.sis.metadata.privy.ImplementationHelper;
import org.apache.sis.io.wkt.Formatter;
import org.apache.sis.io.wkt.Convention;
import org.apache.sis.util.ComparisonMode;

// Specific to the geoapi-3.1 and geoapi-4.0 branches:
import org.opengis.metadata.Identifier;

// Specific to the geoapi-4.0 branch:
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.apache.sis.io.wkt.ElementKind;


/**
 * Defines the origin of an image coordinate reference system. An image datum is used in a local
 * context only. For an image datum, the anchor point is usually either the centre of the image
 * or the corner of the image.
 *
 * <h2>Immutability and thread safety</h2>
 * This class is immutable and thus thread-safe if the property <em>values</em> (not necessarily the map itself)
 * given to the constructor are also immutable. Unless otherwise noted in the javadoc, this condition holds if
 * all components were created using only SIS factories and static constants.
 *
 * @deprecated The {@code ImageDatum} class has been removed in ISO 19111:2019.
 *             It is replaced by {@code EngineeringDatum}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.5
 *
 * @see org.apache.sis.referencing.crs.DefaultImageCRS
 * @see org.apache.sis.referencing.factory.GeodeticAuthorityFactory#createImageDatum(String)
 *
 * @since 0.4
 */
@Deprecated(since="1.5", forRemoval=true)   // Actually to be moved to an internal package for GML and WKT purposes.
@XmlType(name = "ImageDatumType")
@XmlRootElement(name = "ImageDatum")
public final class DefaultImageDatum extends AbstractDatum {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -4304193511244150936L;

    /**
     * Specification of the way the image grid is associated with the image data attributes.
     *
     * <p><b>Consider this field as final!</b>
     * This field is modified only at unmarshalling time by {@link #setPixelInCell(PixelInCell)}</p>
     *
     * @see #getPixelInCell()
     */
    private String pixelInCell;

    /**
     * Creates an image datum from the given properties. The properties map is given
     * unchanged to the {@linkplain AbstractDatum#AbstractDatum(Map) super-class constructor}.
     * The following table is a reminder of main (not all) properties:
     *
     * <table class="sis">
     *   <caption>Recognized properties (non exhaustive list)</caption>
     *   <tr>
     *     <th>Property name</th>
     *     <th>Value type</th>
     *     <th>Returned by</th>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#NAME_KEY}</td>
     *     <td>{@link Identifier} or {@link String}</td>
     *     <td>{@link #getName()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#ALIAS_KEY}</td>
     *     <td>{@link GenericName} or {@link CharSequence} (optionally as array)</td>
     *     <td>{@link #getAlias()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#IDENTIFIERS_KEY}</td>
     *     <td>{@link Identifier} (optionally as array)</td>
     *     <td>{@link #getIdentifiers()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#DOMAINS_KEY}</td>
     *     <td>{@link org.opengis.referencing.ObjectDomain} (optionally as array)</td>
     *     <td>{@link #getDomains()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#REMARKS_KEY}</td>
     *     <td>{@link InternationalString} or {@link String}</td>
     *     <td>{@link #getRemarks()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#ANCHOR_DEFINITION_KEY}</td>
     *     <td>{@link InternationalString} or {@link String}</td>
     *     <td>{@link #getAnchorDefinition()}</td>
     *   </tr><tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#ANCHOR_EPOCH_KEY}</td>
     *     <td>{@link java.time.temporal.Temporal}</td>
     *     <td>{@link #getAnchorEpoch()}</td>
     *   </tr>
     * </table>
     *
     * @param  properties   the properties to be given to the identified object.
     * @param  pixelInCell  the way the image grid is associated with the image data attributes.
     *
     * @see org.apache.sis.referencing.factory.GeodeticObjectFactory#createImageDatum(Map, PixelInCell)
     */
    public DefaultImageDatum(final Map<String,?> properties, final String pixelInCell) {
        super(properties);
        this.pixelInCell = Objects.requireNonNull(pixelInCell);
    }

    /**
     * Specification of the way the image grid is associated with the image data attributes.
     *
     * @return the way image grid is associated with image data attributes.
     */
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String getPixelInCell() {
        return pixelInCell;
    }

    /**
     * Compares this datum with the specified object for equality.
     *
     * @param  object  the object to compare to {@code this}.
     * @param  mode {@link ComparisonMode#STRICT STRICT} for performing a strict comparison, or
     *         {@link ComparisonMode#IGNORE_METADATA IGNORE_METADATA} for comparing only properties
     *         relevant to coordinate transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true;        // Slight optimization.
        }
        return (object instanceof DefaultImageDatum) &&
                Objects.equals(pixelInCell, ((DefaultImageDatum) object).pixelInCell);
    }

    /**
     * Invoked by {@code hashCode()} for computing the hash code when first needed.
     * See {@link org.apache.sis.referencing.AbstractIdentifiedObject#computeHashCode()}
     * for more information.
     *
     * @return the hash code value. This value may change in any future Apache SIS version.
     */
    @Override
    protected long computeHashCode() {
        return super.computeHashCode() + Objects.hashCode(pixelInCell);
    }

    /**
     * Formats this datum as a <i>Well Known Text</i> {@code ImageDatum[…]} element.
     *
     * <h4>Compatibility note</h4>
     * {@code ImageDatum} is defined in the WKT 2 specification only.
     *
     * @return {@code "ImageDatum"}.
     *
     * @see <a href="http://docs.opengeospatial.org/is/12-063r5/12-063r5.html#81">WKT 2 specification §12.2</a>
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        super.formatTo(formatter);
        final Convention convention = formatter.getConvention();
        if (convention == Convention.INTERNAL) {
            formatter.append(getPixelInCell(), ElementKind.CODE_LIST);    // This is an extension compared to ISO 19162.
        } else if (convention.majorVersion() == 1) {
            formatter.setInvalidWKT(this, null);
        }
        return formatter.shortOrLong(WKTKeywords.IDatum, WKTKeywords.ImageDatum);
    }




    /*
     ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     ┃                                                                                  ┃
     ┃                               XML support with JAXB                              ┃
     ┃                                                                                  ┃
     ┃        The following methods are invoked by JAXB using reflection (even if       ┃
     ┃        they are private) or are helpers for other methods invoked by JAXB.       ┃
     ┃        Those methods can be safely removed if Geographic Markup Language         ┃
     ┃        (GML) support is not needed.                                              ┃
     ┃                                                                                  ┃
     ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     */

    /**
     * Constructs a new datum in which every attributes are set to a null value.
     * <strong>This is not a valid object.</strong> This constructor is strictly
     * reserved to JAXB, which will assign values to the fields using reflection.
     */
    private DefaultImageDatum() {
    }

    /**
     * Invoked by JAXB only at unmarshalling time.
     *
     * @see #getPixelInCell()
     */
    private void setPixelInCell(final String value) {
        if (pixelInCell == null) {
            pixelInCell = value;
        } else {
            ImplementationHelper.propertyAlreadySet(DefaultImageDatum.class, "setPixelInCell", "pixelInCell");
        }
    }
}
