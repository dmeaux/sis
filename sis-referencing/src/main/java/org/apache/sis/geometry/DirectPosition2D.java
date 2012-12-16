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
package org.apache.sis.geometry;

import java.util.Objects;
import java.awt.geom.Point2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.apache.sis.util.resources.Errors;

import static java.lang.Double.doubleToLongBits;
import static org.apache.sis.util.StringBuilders.trimFractionalPart;


/**
 * Holds the coordinates for a two-dimensional position within some coordinate reference system.
 * This class inherits {@linkplain #x x} and {@linkplain #y y} fields. But despite their names,
 * they don't need to be oriented toward {@linkplain AxisDirection#EAST East} and {@linkplain
 * AxisDirection#NORTH North}. The (<var>x</var>,<var>y</var>) axis can have any orientation and
 * should be understood as "<cite>ordinate 0</cite>" and "<cite>ordinate 1</cite>" values instead.
 * This is not specific to this implementation; in Java2D too, the visual axis orientation depend
 * on the {@linkplain java.awt.Graphics2D#getTransform() affine transform in the graphics context}.
 *
 * {@note The rational for avoiding axis orientation restriction is that other <code>DirectPosition</code>
 *        implementations do not have such restriction, and it would be hard to generalize.
 *        For example there is no clear "x" or "y" classification for North-East direction.}
 *
 * {@section Caution when used in collections}
 * Do not mix instances of this class with ordinary {@link Point2D} instances
 * in a {@code HashSet} or as {@code HashMap} keys.
 * It is not possible to meet both {@link Point2D#hashCode()} and {@link DirectPosition#hashCode()}
 * contracts, and this class chooses to implements the later. Consequently, the {@link #hashCode()}
 * method of this class is inconsistent with {@link Point2D#equals(Object)} but is consistent with
 * {@link DirectPosition#equals(Object)}.
 *
 * <p>In other words, it is safe to add instances of {@code DirectPosition2D} in a
 * {@code HashSet<DirectPosition>}, but it is unsafe to add them in a {@code HashSet<Point2D>}.
 * Collections that do not rely on hash codes, like {@code ArrayList}, are safe in all cases.</p>
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @since   0.3 (derived from geotk-2.0)
 * @version 0.3
 * @module
 *
 * @see DirectPosition1D
 * @see GeneralDirectPosition
 * @see Point2D
 */
public class DirectPosition2D extends Point2D.Double implements DirectPosition, Cloneable {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 835130287438466996L;

    /**
     * The coordinate reference system for this position;
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs a position initialized to (0,0) with a {@code null} coordinate reference system.
     */
    public DirectPosition2D() {
    }

    /**
     * Constructs a position with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system, or {@code null}.
     */
    public DirectPosition2D(final CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Constructs a 2D position from the specified ordinates. Despite their names,
     * the (<var>x</var>,<var>y</var>) coordinates don't need to be oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}).
     * Those parameter names simply match the {@linkplain #x x} and {@linkplain #y y} fields.
     * See the <a href="#skip-navbar_top">class javadoc</a> for details.
     *
     * @param x The first ordinate value (not necessarily horizontal).
     * @param y The second ordinate value (not necessarily vertical).
     */
    public DirectPosition2D(final double x, final double y) {
        super(x, y);
    }

    /**
     * Constructs a 2D position from the specified ordinates in the specified CRS.
     * Despite their names, the (<var>x</var>,<var>y</var>) coordinates don't need to be oriented
     * toward ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}).
     * Those parameter names simply match the {@linkplain #x x} and {@linkplain #y y} fields.
     * The actual axis orientations are determined by the specified CRS.
     * See the <a href="#skip-navbar_top">class javadoc</a> for details.
     *
     * @param crs The coordinate reference system, or {@code null}.
     * @param x The first ordinate value (not necessarily horizontal).
     * @param y The second ordinate value (not necessarily vertical).
     */
    public DirectPosition2D(final CoordinateReferenceSystem crs,
                            final double x, final double y)
    {
        super(x, y);
        AbstractDirectPosition.ensureDimensionMatch(crs, 2);
        this.crs = crs;
    }

    /**
     * Constructs a position from the specified {@link Point2D}.
     * If the given point implements also the {@code DirectPosition} interface, then this
     * constructor will set the CRS of this {@code DirectPosition2D} to the CRS of the given point.
     * Otherwise the CRS of this {@code DirectPosition2D} will be initially null.
     *
     * @param point The point to copy.
     */
    public DirectPosition2D(final Point2D point) {
        super(point.getX(), point.getY());
        if (point instanceof DirectPosition) {
            crs = ((DirectPosition) point).getCoordinateReferenceSystem();
            AbstractDirectPosition.ensureDimensionMatch(crs, 2);
        }
    }

    /**
     * Constructs a position initialized to the values parsed from the given string in
     * <cite>Well Known Text</cite> (WKT) format. The given string is typically a {@code POINT}
     * element like below:
     *
     * {@preformat wkt
     *     POINT(6 10)
     * }
     *
     * @param  wkt The {@code POINT} or other kind of element to parse.
     * @throws IllegalArgumentException If the given string can not be parsed.
     * @throws MismatchedDimensionException If the given point is not two-dimensional.
     *
     * @see #toString()
     * @see org.geotoolkit.measure.CoordinateFormat
     */
    public DirectPosition2D(final String wkt) throws IllegalArgumentException {
        final double[] ordinates = AbstractDirectPosition.parse(wkt);
        if (ordinates == null) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.UnparsableStringForClass_2, "POINT", wkt));
        }
        AbstractDirectPosition.ensureDimensionMatch("wkt", ordinates.length, 2);
        x = ordinates[0];
        y = ordinates[1];
    }

    /**
     * Returns always {@code this}, the direct position for this
     * {@linkplain org.opengis.geometry.coordinate.Position position}.
     */
    @Override
    public final DirectPosition getDirectPosition() {
        return this;
    }

    /**
     * The length of coordinate sequence (the number of entries).
     * This is always 2 for {@code DirectPosition2D} objects.
     *
     * @return The dimensionality of this position.
     */
    @Override
    public final int getDimension() {
        return 2;
    }

    /**
     * Returns the coordinate reference system in which the coordinate is given.
     * May be {@code null} if this particular {@code DirectPosition} is included
     * in a larger object with such a reference to a CRS.
     *
     * @return The coordinate reference system, or {@code null}.
     */
    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Sets the coordinate reference system in which the coordinate is given.
     *
     * @param crs The new coordinate reference system, or {@code null}.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        AbstractDirectPosition.ensureDimensionMatch(crs, 2);
        this.crs = crs;
    }

    /**
     * Returns a sequence of numbers that hold the coordinate of this position in its
     * reference system.
     *
     * {@note This method is final for ensuring consistency with the <code>x</code>
     *        and <code>y</code> fields, which are public.}
     *
     * @return The coordinate.
     */
    @Override
    public final double[] getCoordinate() {
        return new double[] {x,y};
    }

    /**
     * Returns the ordinate at the specified dimension.
     *
     * {@note This method is final for ensuring consistency with the <code>x</code>
     *        and <code>y</code> fields, which are public.}
     *
     * @param  dimension The dimension in the range 0 to 1 inclusive.
     * @return The coordinate at the specified dimension.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    @Override
    public final double getOrdinate(final int dimension) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:  return x;
            case 1:  return y;
            default: throw new IndexOutOfBoundsException(Errors.format(Errors.Keys.IndexOutOfBounds_1, dimension));
        }
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param  dimension the dimension for the ordinate of interest.
     * @param  value the ordinate value of interest.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    @Override
    public void setOrdinate(int dimension, double value) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:  x = value; break;
            case 1:  y = value; break;
            default: throw new IndexOutOfBoundsException(Errors.format(Errors.Keys.IndexOutOfBounds_1, dimension));
        }
    }

    /**
     * Sets this coordinate to the specified direct position. If the specified position
     * contains a coordinate reference system (CRS), then the CRS for this position will
     * be set to the CRS of the specified position.
     *
     * @param  position The new position for this point.
     * @throws MismatchedDimensionException if this point doesn't have the expected dimension.
     */
    public void setLocation(final DirectPosition position) throws MismatchedDimensionException {
        AbstractDirectPosition.ensureDimensionMatch("position", position.getDimension(), 2);
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        x = position.getOrdinate(0);
        y = position.getOrdinate(1);
    }

    /**
     * Formats this position in the <cite>Well Known Text</cite> (WKT) format.
     * The output is like below:
     *
     * {@preformat wkt
     *   POINT(x y)
     * }
     *
     * The string returned by this method can be {@linkplain #DirectPosition2D(String) parsed}
     * by the {@code DirectPosition2D} constructor.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(32);
        trimFractionalPart(buffer.append("POINT(").append(x));
        trimFractionalPart(buffer.append(' ').append(y));
        return buffer.append(')').toString();
    }

    /**
     * Returns a hash value for this coordinate. This method implements the
     * {@link DirectPosition#hashCode()} contract, not the {@link Point2D#hashCode()} contract.
     *
     * @return A hash code value for this position.
     */
    @Override
    public int hashCode() {
        int code;
        long bits;
        bits = doubleToLongBits(x); code = 31      + (((int) bits) ^ (int) (bits >>> 32));
        bits = doubleToLongBits(y); code = 31*code + (((int) bits) ^ (int) (bits >>> 32));
        if (crs != null) {
            code += crs.hashCode();
        }
        return code;
    }

    /**
     * Compares this point with the specified object for equality. If the given object implements
     * the {@code DirectPosition} interface, then the comparison is performed as specified in the
     * {@link DirectPosition#equals(Object)} contract. Otherwise the comparison is performed as
     * specified in the {@link Point2D#equals(Object)} contract.
     *
     * @param object The object to compare with this position.
     * @return {@code true} if the given object is equal to this position.
     */
    @Override
    public boolean equals(final Object object) {
        /*
         * If the other object implements the DirectPosition interface, performs
         * the comparison as specified in DirectPosition.equals(Object) contract.
         */
        if (object instanceof DirectPosition) {
            final DirectPosition other = (DirectPosition) object;
            if (other.getDimension() == 2 &&
                doubleToLongBits(other.getOrdinate(0)) == doubleToLongBits(x) &&
                doubleToLongBits(other.getOrdinate(1)) == doubleToLongBits(y) &&
                Objects.equals(other.getCoordinateReferenceSystem(), crs))
            {
                assert hashCode() == other.hashCode() : this;
                return true;
            }
            return false;
        }
        /*
         * Otherwise performs the comparison as in Point2D.equals(Object).
         * Do NOT check the CRS if the given object is an ordinary Point2D.
         * This is necessary in order to respect the contract defined in Point2D.
         */
        return super.equals(object);
    }

    /**
     * Returns a clone of this point.
     *
     * @return A clone of this position.
     */
    @Override
    public DirectPosition2D clone() {
        return (DirectPosition2D) super.clone();
    }
}
