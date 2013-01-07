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
package org.apache.sis.measure;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.measure.unit.SI;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;
import javax.measure.converter.UnitConverter;
import org.apache.sis.util.Static;
import org.apache.sis.util.Workaround;
import org.apache.sis.util.Arrays;
import org.apache.sis.util.Exceptions;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.resources.Errors;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static org.apache.sis.measure.SexagesimalConverter.EPS;
import static org.apache.sis.util.CharSequences.trimWhitespaces;


/**
 * Static methods working on {@link Unit} instances, and some constants in addition to the
 * {@link SI} and {@link NonSI} ones.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
public final class Units extends Static {
    /**
     * The suffixes that NetCDF files sometime put after the "degrees" unit.
     * Suffix at even index are for axes having the standard geometric direction,
     * while suffix at odd index are for axes having the reverse direction.
     */
    private static final String[] DEGREE_SUFFIXES = {"east", "west", "north", "south"};

    /**
     * Do not allows instantiation of this class.
     */
    private Units() {
    }

    /**
     * Unit for milliseconds. Useful for conversion from and to {@link java.util.Date} objects.
     */
    public static final Unit<Duration> MILLISECOND = SI.MetricPrefix.MILLI(SI.SECOND);

    /**
     * Pseudo-unit for sexagesimal degree. Numbers in this pseudo-unit have the following format:
     *
     * <cite>sign - degrees - decimal point - minutes (two digits) - integer seconds (two digits) -
     * fraction of seconds (any precision)</cite>.
     *
     * Using this unit is loosely equivalent to formatting decimal degrees with the
     * {@code "D.MMSSs"} {@link AngleFormat} pattern.
     *
     * <p>This unit is non-linear and not practical for computation. Consequently, it should be
     * avoided as much as possible. This pseudo-unit is defined only because extensively used in
     * the EPSG database (code 9110).</p>
     *
     * <p>This unit does not have an easily readable symbol because of the
     * <a href="http://kenai.com/jira/browse/JSR_275-41">JSR-275 bug</a>.</p>
     */
    static final Unit<Angle> SEXAGESIMAL_DMS = NonSI.DEGREE_ANGLE.transform(
            SexagesimalConverter.FRACTIONAL.inverse()).asType(Angle.class);//.alternate("D.MS");

    /**
     * Pseudo-unit for degree - minute - second.
     * Numbers in this pseudo-unit have the following format:
     *
     * <cite>signed degrees (integer) - arc-minutes (integer) - arc-seconds
     * (real, any precision)</cite>.
     *
     * Using this unit is loosely equivalent to formatting decimal degrees with the
     * {@code "DMMSS.s"} {@link AngleFormat} pattern.
     *
     * <p>This unit is non-linear and not practical for computation. Consequently, it should be
     * avoided as much as possible. This pseudo-unit is defined only because extensively used in
     * EPSG database (code 9107).</p>
     *
     * <p>This unit does not have an easily readable symbol because of the
     * <a href="http://kenai.com/jira/browse/JSR_275-41">JSR-275 bug</a>.</p>
     */
    static final Unit<Angle> DEGREE_MINUTE_SECOND = NonSI.DEGREE_ANGLE.transform(
            SexagesimalConverter.INTEGER.inverse()).asType(Angle.class);//.alternate("DMS");

    /**
     * Parts per million.
     *
     * <p>This unit does not have an easily readable symbol because of the
     * <a href="http://kenai.com/jira/browse/JSR_275-41">JSR-275 bug</a>.</p>
     */
    public static final Unit<Dimensionless> PPM = Unit.ONE.times(1E-6);//.alternate("ppm");

    /**
     * A few units commonly used in GIS.
     */
    private static final Map<Unit<?>,Unit<?>> COMMONS = new HashMap<Unit<?>,Unit<?>>(48);
    static {
        COMMONS.put(PPM, PPM);
        boolean nonSI = false;
        do for (final Field field : (nonSI ? NonSI.class : SI.class).getFields()) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                final Object value;
                try {
                    value = field.get(null);
                } catch (Exception e) { // (ReflectiveOperationException) on JDK7
                    // Should not happen since we asked only for public static constants.
                    throw new AssertionError(e);
                }
                if (value instanceof Unit<?>) {
                    final Unit<?> unit = (Unit<?>) value;
                    if (isLinear(unit) || isAngular(unit) || isScale(unit)) {
                        COMMONS.put(unit, unit);
                    }
                }
            }
        } while ((nonSI = !nonSI) == true);
    }

    /**
     * Returns {@code true} if the given unit is a linear unit.
     * Linear units are convertible to {@link NonSI#DEGREE_ANGLE}.
     *
     * <p>Angular units are dimensionless, which may be a cause of confusion with other
     * dimensionless units like {@link Unit#ONE} or {@link #PPM}. This method take care
     * of differentiating angular units from other dimensionless units.</p>
     *
     * @param unit The unit to check (may be {@code null}).
     * @return {@code true} if the given unit is non-null and angular.
     *
     * @see #ensureAngular(Unit)
     */
    public static boolean isAngular(final Unit<?> unit) {
        return (unit != null) && unit.toSI().equals(SI.RADIAN);
    }

    /**
     * Returns {@code true} if the given unit is a linear unit.
     * Linear units are convertible to {@link SI#METRE}.
     *
     * @param unit The unit to check (may be {@code null}).
     * @return {@code true} if the given unit is non-null and linear.
     *
     * @see #ensureLinear(Unit)
     */
    public static boolean isLinear(final Unit<?> unit) {
        return (unit != null) && unit.toSI().equals(SI.METRE);
    }

    /**
     * Returns {@code true} if the given unit is a pressure unit.
     * Pressure units are convertible to {@link SI#PASCAL}.
     * Those units are sometime used instead of linear units for altitude measurements.
     *
     * @param unit The unit to check (may be {@code null}).
     * @return {@code true} if the given unit is non-null and a pressure unit.
     */
    public static boolean isPressure(final Unit<?> unit) {
        return (unit != null) && unit.toSI().equals(SI.PASCAL);
    }

    /**
     * Returns {@code true} if the given unit is a temporal unit.
     * Temporal units are convertible to {@link SI#SECOND}.
     *
     * @param unit The unit to check (may be {@code null}).
     * @return {@code true} if the given unit is non-null and temporal.
     *
     * @see #ensureTemporal(Unit)
     */
    public static boolean isTemporal(final Unit<?> unit) {
        return (unit != null) && unit.toSI().equals(SI.SECOND);
    }

    /**
     * Returns {@code true} if the given unit is a dimensionless scale unit.
     * This include {@link Unit#ONE} and {@link #PPM}.
     *
     * @param unit The unit to check (may be {@code null}).
     * @return {@code true} if the given unit is non-null and a dimensionless scale.
     *
     * @see #ensureScale(Unit)
     */
    public static boolean isScale(final Unit<?> unit) {
        return (unit != null) && unit.toSI().equals(Unit.ONE);
    }

    /**
     * Makes sure that the specified unit is either null or an angular unit.
     * This method is used for argument checks in constructors and setter methods.
     *
     * @param  unit The unit to check, or {@code null} if none.
     * @return The given {@code unit} argument, which may be null.
     * @throws IllegalArgumentException if {@code unit} is non-null and not an angular unit.
     *
     * @see #isAngular(Unit)
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static Unit<Angle> ensureAngular(final Unit<?> unit) throws IllegalArgumentException {
        if (unit != null && !isAngular(unit)) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.NonAngularUnit_1, unit));
        }
        return (Unit) unit;
    }

    /**
     * Makes sure that the specified unit is either null or a linear unit.
     * This method is used for argument checks in constructors and setter methods.
     *
     * @param  unit The unit to check, or {@code null} if none.
     * @return The given {@code unit} argument, which may be null.
     * @throws IllegalArgumentException if {@code unit} is non-null and not a linear unit.
     *
     * @see #isLinear(Unit)
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static Unit<Length> ensureLinear(final Unit<?> unit) throws IllegalArgumentException {
        if (unit != null && !isLinear(unit)) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.NonLinearUnit_1, unit));
        }
        return (Unit) unit;
    }

    /**
     * Makes sure that the specified unit is either null or a temporal unit.
     * This method is used for argument checks in constructors and setter methods.
     *
     * @param  unit The unit to check, or {@code null} if none.
     * @return The given {@code unit} argument, which may be null.
     * @throws IllegalArgumentException if {@code unit} is non-null and not a temporal unit.
     *
     * @see #isTemporal(Unit)
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static Unit<Duration> ensureTemporal(final Unit<?> unit) throws IllegalArgumentException {
        if (unit != null && !isTemporal(unit)) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.NonTemporalUnit_1, unit));
        }
        return (Unit) unit;
    }

    /**
     * Makes sure that the specified unit is either null or a scale unit.
     * This method is used for argument checks in constructors and setter methods.
     *
     * @param  unit The unit to check, or {@code null} if none.
     * @return The given {@code unit} argument, which may be null.
     * @throws IllegalArgumentException if {@code unit} is non-null and not a scale unit.
     *
     * @see #isScale(Unit)
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static Unit<Dimensionless> ensureScale(final Unit<?> unit) throws IllegalArgumentException {
        if (unit != null && !isScale(unit)) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.NonScaleUnit_1, unit));
        }
        return (Unit) unit;
    }

    /**
     * Multiplies the given unit by the given factor. For example multiplying {@link SI#METRE}
     * by 1000 gives {@link SI#KILOMETRE}. Invoking this method is equivalent to invoking
     * {@link Unit#times(double)} except for the following:
     *
     * <ul>
     *   <li>A small tolerance factor is applied for a few factors commonly used in GIS.
     *       For example {@code multiply(SI.RADIANS, 0.0174532925199...)} will return
     *       {@link NonSI#DEGREE_ANGLE} even if the given numerical value is slightly
     *       different than {@linkplain Math#PI pi}/180. The tolerance factor and the
     *       set of units handled especially may change in future SIS versions.</li>
     *   <li>This method tries to returns unique instances for some common units.</li>
     * </ul>
     *
     * @param  <A>    The quantity measured by the unit.
     * @param  unit   The unit to multiply.
     * @param  factor The multiplication factor.
     * @return The unit multiplied by the given factor.
     */
    @Workaround(library="JSR-275", version="0.9.3")
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <A extends Quantity> Unit<A> multiply(Unit<A> unit, final double factor) {
        if (SI.RADIAN.equals(unit)) {
            if (abs(factor - (PI / 180)) <= (EPS * PI/180)) {
                return (Unit) NonSI.DEGREE_ANGLE;
            }
            if (abs(factor - (PI / 200)) <= (EPS * PI/200)) {
                return (Unit) NonSI.GRADE;
            }
        }
        if (abs(factor - 1) > EPS) {
            final long fl = (long) factor;
            if (fl == factor) {
                /*
                 * Invoke the Unit.times(long) overloaded method, not Unit.scale(double),
                 * because as of JSR-275 0.9.3 the method with the long argument seems to
                 * do a better work of detecting when the result is an existing unit.
                 */
                unit = unit.times(fl);
            } else {
                unit = unit.times(factor);
            }
        }
        return canonicalize(unit);
    }

    /**
     * Returns a unique instance of the given units if possible, or the units unchanged otherwise.
     *
     * @param  <A>    The quantity measured by the unit.
     * @param  unit   The unit to canonicalize.
     * @return A unit equivalents to the given unit, canonicalized if possible.
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    private static <A extends Quantity> Unit<A> canonicalize(final Unit<A> unit) {
        final Unit<?> candidate = COMMONS.get(unit);
        if (candidate != null) {
            return (Unit) candidate;
        }
        return unit;
    }

    /**
     * Returns the factor by which to multiply the standard unit in order to get the given unit.
     * The "standard" unit is usually the SI unit on which the given unit is based.
     *
     * <p><b>Example:</b> If the given unit is <var>kilometre</var>, then this method returns 1000
     * since a measurement in kilometres must be multiplied by 1000 in order to give the equivalent
     * measurement in the "standard" units (here <var>metres</var>).</p>
     *
     * @param  <A>  The quantity measured by the unit.
     * @param  unit The unit for which we want the multiplication factor to standard unit.
     * @return The factor by which to multiply a measurement in the given unit in order to
     *         get an equivalent measurement in the standard unit.
     */
    @Workaround(library="JSR-275", version="0.9.3")
    public static <A extends Quantity> double toStandardUnit(final Unit<A> unit) {
        return derivative(unit.getConverterTo(unit.toSI()), 0);
    }

    /**
     * Returns an estimation of the derivative of the given converter at the given value.
     * This method is a workaround for a method which existed in previous JSR-275 API but
     * have been removed in more recent releases. This method will be deprecated if the
     * removed API is reinserted in future JSR-275 release.
     *
     * <p>Current implementation computes the derivative as below:</p>
     *
     * {@preformat java
     *     return converter.convert(value + 1) - converter.convert(value);
     * }
     *
     * The above is exact for {@linkplain javax.measure.converter.LinearConverter linear converters},
     * which is the case of the vast majority of unit converters in use. It may not be exact for a
     * few unusual converter like the one from {@link #SEXAGESIMAL_DMS} to decimal degrees for
     * example.
     *
     * @param  converter The converter for which we want the derivative at a given point.
     * @param  value The point at which to compute the derivative.
     * @return The derivative at the given point.
     */
    @Workaround(library="JSR-275", version="0.9.3")
    public static double derivative(final UnitConverter converter, final double value) {
        return converter.convert(value + 1) - converter.convert(value);
    }

    /**
     * Parses the given symbol. This method is similar to {@link Unit#valueOf(CharSequence)}, but
     * hands especially a few symbols found in WKT parsing or in XML files. The list of symbols
     * handled especially is implementation-dependent and may change in future SIS versions.
     *
     * {@section Parsing authority codes}
     * As a special case, if the given {@code uom} arguments is of the form {@code "EPSG:####"}
     * (ignoring case and whitespaces), then {@code "####"} is parsed as an integer and forwarded
     * to the {@link #valueOfEPSG(int)} method.
     *
     * {@section NetCDF unit symbols}
     * The attributes in NetCDF files often merge the axis direction with the angular unit,
     * as in {@code "degrees_east"} or {@code "degrees_north"}. This {@code valueOf} method
     * ignores those suffixes and unconditionally returns {@link NonSI#DEGREE_ANGLE} for all
     * axis directions. In particular, the units for {@code "degrees_west"} and {@code "degrees_east"}
     * do <strong>not</strong> have opposite sign. It is caller responsibility to handle the
     * direction of axes associated to NetCDF units.
     *
     * @param  uom The symbol to parse, or {@code null}.
     * @return The parsed symbol, or {@code null} if {@code uom} was null.
     * @throws IllegalArgumentException if the given symbol can not be parsed.
     */
    public static Unit<?> valueOf(String uom) throws IllegalArgumentException {
        if (uom == null) {
            return null;
        }
        uom = trimWhitespaces(CharSequences.toASCII(uom)).toString();
        final int length = uom.length();
        /*
         * Check for authority codes (currently only EPSG, but more could be added later).
         * If the unit is not an authority code (which is the most common case), then we
         * will check for hard-coded unit symbols.
         */
        int s = uom.indexOf(':');
        if (s >= 0) {
            final String authority = (String) trimWhitespaces(uom, 0, s);
            if (authority.equalsIgnoreCase("EPSG")) try {
                return valueOfEPSG(Integer.parseInt((String) trimWhitespaces(uom, s+1, length)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(Errors.format(
                        Errors.Keys.IllegalArgumentValue_2, "uom", uom), e);
            }
        }
        /*
         * Check for degrees units. Note that "deg" could be both angular and Celsius degrees.
         * We try to resolve this ambiguity in the code below by looking for the "Celsius" suffix.
         * Other suffixes commonly found in NetCDF files are "west", "east", "north" or "south".
         * Those suffixes are ignored.
         */
        if (uom.regionMatches(true, 0, "deg", 0, 3)) {
            if (length == 3) {
                return NonSI.DEGREE_ANGLE; // Exactly "deg"
            }
            String prefix = uom;
            boolean isTemperature = false;
            s = Math.max(uom.lastIndexOf(' '), uom.lastIndexOf('_'));
            if (s >= 1) {
                final String suffix = (String) trimWhitespaces(uom, s+1, length);
                if (Arrays.containsIgnoreCase(DEGREE_SUFFIXES, suffix) || (isTemperature = isCelsius(suffix))) {
                    prefix = (String) trimWhitespaces(uom, 0, s); // Remove the suffix only if we recognized it.
                }
            }
            if (equalsIgnorePlural(prefix, "degree")) {
                return isTemperature ? SI.CELSIUS : NonSI.DEGREE_ANGLE;
            }
        } else {
            /*
             * Check for unit symbols that do not begin with "deg". If a symbol begins
             * with "deg", then the check should be put in the above block instead.
             */
            if (uom.equals("°")                      || equalsIgnorePlural(uom, "decimal_degree")) return NonSI.DEGREE_ANGLE;
            if (uom.equalsIgnoreCase("rad")          || equalsIgnorePlural(uom, "radian"))         return SI.RADIAN;
            if (equalsIgnorePlural(uom, "kilometer") || equalsIgnorePlural(uom, "kilometre"))      return SI.KILOMETRE;
            if (equalsIgnorePlural(uom, "meter")     || equalsIgnorePlural(uom, "metre"))          return SI.METRE;
            if (equalsIgnorePlural(uom, "week"))   return NonSI.WEEK;
            if (equalsIgnorePlural(uom, "day"))    return NonSI.DAY;
            if (equalsIgnorePlural(uom, "hour"))   return NonSI.HOUR;
            if (equalsIgnorePlural(uom, "minute")) return NonSI.MINUTE;
            if (equalsIgnorePlural(uom, "second")) return SI   .SECOND;
            if (equalsIgnorePlural(uom, "pixel"))  return NonSI.PIXEL;
            if (isCelsius(uom))                    return SI.CELSIUS;
            if (uom.isEmpty() ||
                uom.equalsIgnoreCase("psu") ||   // Pratical Salinity Scale (oceanography)
                uom.equalsIgnoreCase("level"))   // Sigma level (oceanography)
            {
                return Unit.ONE;
            }
        }
        final Unit<?> unit;
        try {
            unit = Unit.valueOf(uom);
        } catch (IllegalArgumentException e) {
            // Provides a better error message than the default JSR-275 0.9.4 implementation.
            throw Exceptions.setMessage(e, Errors.format(Errors.Keys.IllegalArgumentValue_2, "uom", uom), true);
        }
        return canonicalize(unit);
    }

    /**
     * Returns {@code true} if the given {@code uom} is equals to the given expected string,
     * ignoring trailing {@code 's'} character (if any).
     */
    @SuppressWarnings("fallthrough")
    private static boolean equalsIgnorePlural(final String uom, final String expected) {
        final int length = expected.length();
        switch (uom.length() - length) {
            case 0:  break; // uom has exactly the expected length.
            case 1:  if (Character.toLowerCase(uom.charAt(length)) == 's') break; // else fallthrough.
            default: return false;
        }
        return uom.regionMatches(true, 0, expected, 0, length);
    }

    /**
     * Returns {@code true} if the given {@code uom} is equals to {@code "Celsius"} or
     * {@code "Celcius"}. The later is a common misspelling.
     */
    private static boolean isCelsius(final String uom) {
        return uom.equalsIgnoreCase("Celsius") || uom.equalsIgnoreCase("Celcius");
    }

    /**
     * Returns a hard-coded unit from an EPSG code. The {@code code} argument given to this
     * method shall be a code identifying a record in the {@code "Unit of Measure"} table of
     * the EPSG database. If this method does not recognize the given code, then it returns
     * {@code null}.
     *
     * <p>The list of units recognized by this method is not exhaustive. This method recognizes
     * the base units declared in the {@code [TARGET_UOM_CODE]} column of the above-cited table,
     * and some frequently-used units. The list of recognized units may be updated in any future
     * version of SIS.</p>
     *
     * <p>The {@link org.apache.sis.referencing.factory.epsg.DirectEpsgFactory} uses this method
     * for fetching the base units, and derives automatically other units from the information
     * found in the EPSG database. This method is also used by other code not directly related
     * to the EPSG database, like {@link org.apache.sis.referencing.factory.web.AutoCRSFactory}
     * which uses EPSG code for identifying units.</p>
     *
     * <p>The values currently recognized are:</p>
     * <table class="sis">
     *   <tr>
     *     <th>Linear units</th>
     *     <th class="sep">Angular units</th>
     *     <th class="sep">Scale units</th>
     *   </tr><tr>
     *     <td><table class="compact">
     *       <tr><td width="40"><b>Code</b></td><td><b>Unit</b></td></tr>
     *       <tr><td>9001</td><td>metre</td></tr>
     *       <tr><td>9002</td><td>foot</td></tr>
     *       <tr><td>9030</td><td>nautical mile</td></tr>
     *       <tr><td>9036</td><td>kilometre</td></tr>
     *     </table></td>
     *     <td class="sep"><table class="compact">
     *       <tr><td width="40"><b>Code</b></td><td><b>Unit</b></td></tr>
     *       <tr><td>9101</td><td>radian</td></tr>
     *       <tr><td>9102</td><td>decimal degree</td></tr>
     *       <tr><td>9103</td><td>minute</td></tr>
     *       <tr><td>9104</td><td>second</td></tr>
     *       <tr><td>9105</td><td>grade</td></tr>
     *       <tr><td>9107</td><td>degree-minute-second</td></tr>
     *       <tr><td>9108</td><td>degree-minute-second</td></tr>
     *       <tr><td>9109</td><td>microradian</td></tr>
     *       <tr><td>9110</td><td>sexagesimal degree-minute-second</td></tr>
     *       <tr><td>9111</td><td>sexagesimal degree-minute</td></tr>
     *       <tr><td>9122</td><td>decimal degree</td></tr>
     *     </table></td>
     *     <td class="sep"><table class="compact">
     *       <tr><td width="40"><b>Code</b></td><td><b>Unit</b></td></tr>
     *       <tr><td>9201</td><td>one</td></tr>
     *       <tr><td>9202</td><td>part per million</td></tr>
     *       <tr><td>9203</td><td>one</td></tr>
     *     </table></td>
     *   </tr>
     * </table>
     *
     * @param  code The EPSG code for a unit of measurement.
     * @return The unit, or {@code null} if the code is unrecognized.
     */
    public static Unit<?> valueOfEPSG(final int code) {
        switch (code) {
            case 9001: return SI   .METRE;
            case 9002: return NonSI.FOOT;
            case 9030: return NonSI.NAUTICAL_MILE;
            case 9036: return SI   .KILOMETRE;
            case 9101: return SI   .RADIAN;
            case 9122: // Fall through
            case 9102: return NonSI.DEGREE_ANGLE;
            case 9103: return NonSI.MINUTE_ANGLE;
            case 9104: return NonSI.SECOND_ANGLE;
            case 9105: return NonSI.GRADE;
            case 9107: return Units.DEGREE_MINUTE_SECOND;
            case 9108: return Units.DEGREE_MINUTE_SECOND;
            case 9109: return SI.MetricPrefix.MICRO(SI.RADIAN);
            case 9111: // Sexagesimal DM: use DMS.
            case 9110: return Units.SEXAGESIMAL_DMS;
            case 9203: // Fall through
            case 9201: return Unit .ONE;
            case 9202: return Units.PPM;
            default:   return null;
        }
    }
}