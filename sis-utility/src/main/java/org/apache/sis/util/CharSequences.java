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
package org.apache.sis.util;

import static java.lang.Character.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.copyOf;
import static org.apache.sis.util.Arrays.resize;
import static org.apache.sis.util.StringBuilders.replace;


/**
 * Utility methods working on {@link CharSequence} instances. Some methods defined in this
 * class duplicate the functionalities already provided in the standard {@link String} class,
 * but works on a generic {@code CharSequence} instance instead than {@code String}.
 *
 * {@section Unicode support}
 * Every methods defined in this class work on <cite>code points</cite> instead than characters
 * when appropriate. Consequently those methods should behave correctly with characters outside
 * the <cite>Basic Multilingual Plane</cite> (BMP).
 *
 * {@section Handling of null values}
 * Most methods in this class accept a {@code null} {@code CharSequence} argument. In such cases
 * the method return value is either a {@code null} {@code CharSequence}, an empty array, or a
 * {@code int} primitive type calculated as if the input was an empty string.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 * @module
 *
 * @see StringBuilders
 * @see java.util.Arrays#toString(Object[])
 */
public final class CharSequences extends Static {
    /**
     * An array of zero-length. This constant play a role equivalents to
     * {@link java.util.Collections#EMPTY_LIST}.
     */
    public static final String[] EMPTY_ARRAY = new String[0];

    /**
     * An array of strings containing only white spaces. String lengths are equal to their
     * index in the {@code spaces} array. For example, {@code spaces[4]} contains a string
     * of length 4. Strings are constructed only when first needed.
     */
    private static final String[] SPACES = new String[21];
    static {
        // Our 'spaces(int)' method will invoke 'substring' on the longuest string in an attempt
        // to share the same char[] array. Note however that array sharing has been removed from
        // JDK8, which copy every char[] arrays anyway. Consequently the JDK8 branch will abandon
        // this strategy and build the char[] array on the fly.
        final int last = SPACES.length - 1;
        final char[] spaces = new char[last];
        fill(spaces, ' ');
        SPACES[last] = new String(spaces).intern();
    }

    /**
     * Do not allow instantiation of this class.
     */
    private CharSequences() {
    }

    /**
     * Returns the code point after the given index. This method completes
     * {@link Character#codePointBefore(CharSequence, int)} but is rarely used because slightly
     * inefficient (in most cases, the code point at {@code index} is known together with the
     * corresponding {@code charCount(int)} value, so the method calls should be unnecessary).
     */
    private static int codePointAfter(final CharSequence text, final int index) {
        return codePointAt(text, index + charCount(codePointAt(text, index)));
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     * <p>
     * This method is typically used for performing right-alignment of text on the
     * {@linkplain java.io.Console console} or other device using monospaced font.
     * The {@code length} argument is then calculated by (<var>desired width</var> -
     * <var>used width</var>). Since the used width may be greater than expected,
     * this method accepts negative {@code length} values as if they were zero.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length {@code length} filled with white spaces.
     */
    public static String spaces(int length) {
        /*
         * No need to synchronize.  In the unlikely event of two threads calling this method
         * at the same time and the two calls creating a new string, the String.intern() call
         * will take care of canonicalizing the strings.
         */
        if (length < 0) {
            length = 0;
        }
        String s;
        if (length < SPACES.length) {
            s = SPACES[length];
            if (s == null) {
                s = SPACES[SPACES.length - 1].substring(0, length).intern();
                SPACES[length] = s;
            }
        } else {
            final char[] spaces = new char[length];
            fill(spaces, ' ');
            s = new String(spaces);
        }
        return s;
    }

    /**
     * Returns the {@linkplain CharSequence#length() length} of the given characters sequence,
     * or 0 if {@code null}.
     *
     * @param  text The character sequence from which to get the length, or {@code null}.
     * @return The length of the character sequence, or 0 if the argument is {@code null}.
     */
    public static int length(final CharSequence text) {
        return (text != null) ? text.length() : 0;
    }

    /**
     * Returns the number of occurrences of the {@code toSearch} string in the given {@code text}.
     * The search is case-sensitive.
     *
     * @param  text The character sequence to count occurrences, or {@code null}.
     * @param  toSearch The string to search in the given {@code text}.
     *         It shall contain at least one character.
     * @return The number of occurrences of {@code toSearch} in {@code text},
     *         or 0 if {@code text} was null or empty.
     * @throws NullArgumentException If the {@code toSearch} argument is null.
     * @throws IllegalArgumentException If the {@code toSearch} argument is empty.
     */
    public static int count(final CharSequence text, final String toSearch) {
        ArgumentChecks.ensureNonEmpty("toSearch", toSearch);
        final int length = toSearch.length();
        if (length == 1) {
            // Implementation working on a single character is faster.
            return count(text, toSearch.charAt(0));
        }
        int n = 0;
        if (text != null) {
            int i = 0;
            while ((i = indexOf(text, toSearch, i)) >= 0) {
                n++;
                i += length;
            }
        }
        return n;
    }

    /**
     * Counts the number of occurrence of the given character in the given character sequence.
     *
     * @param  text The character sequence to count occurrences, or {@code null}.
     * @param  toSearch The character to count.
     * @return The number of occurrences of the given character, or 0 if the {@code text} is null.
     */
    public static int count(final CharSequence text, final char toSearch) {
        int n = 0;
        if (text != null) {
            if (text instanceof String) {
                final String s = (String) text;
                for (int i=s.indexOf(toSearch); ++i != 0; i=s.indexOf(toSearch, i)) {
                    n++;
                }
            } else {
                // No need to use the code point API here, since we are looking for exact matches.
                for (int i=text.length(); --i>=0;) {
                    if (text.charAt(i) == toSearch) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /**
     * Splits a text around the given character. The array returned by this method contains all
     * subsequences of the given text that is terminated by the given character or is terminated
     * by the end of the text. The subsequences in the array are in the order in which they occur
     * in the given text. If the character is not found in the input, then the resulting array has
     * just one element, which is the whole given text.
     * <p>
     * This method is similar to the standard {@link String#split(String)} method except for the
     * following:
     * <p>
     * <ul>
     *   <li>It accepts generic character sequences.</li>
     *   <li>It accepts {@code null} argument, in which case an empty array is returned.</li>
     *   <li>The separator is a simple character instead than a regular expression.</li>
     *   <li>The leading and trailing spaces of each subsequences are {@linkplain #trimWhitespaces trimmed}.</li>
     * </ul>
     *
     * @param  toSplit   The text to split, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @return The array of subsequences computed by splitting the given text around the given
     *         character, or an empty array if {@code toSplit} was null.
     *
     * @see String#split(String)
     */
    public static CharSequence[] split(final CharSequence toSplit, final char separator) {
        if (toSplit == null) {
            return EMPTY_ARRAY;
        }
        // 'excludeEmpty' must use the same criterion than trimWhitespaces(...).
        final boolean excludeEmpty = isWhitespace(separator);
        CharSequence[] strings = new CharSequence[4];
        int count = 0, last  = 0, i = 0;
        while ((i = indexOf(toSplit, separator, i)) >= 0) {
            // Note: parseDoubles(...) needs the call to trimWhitespaces(...).
            final CharSequence item = trimWhitespaces(toSplit.subSequence(last, i));
            if (!excludeEmpty || item.length() != 0) {
                if (count == strings.length) {
                    strings = copyOf(strings, count << 1);
                }
                strings[count++] = item;
            }
            last = ++i;
        }
        // Add the last element.
        final CharSequence item = trimWhitespaces(toSplit.subSequence(last, toSplit.length()));
        if (!excludeEmpty || item.length() != 0) {
            if (count == strings.length) {
                strings = copyOf(strings, count + 1);
            }
            strings[count++] = item;
        }
        return resize(strings, count);
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Double#parseDouble(String) parses} each item as a {@code double}.
     * Empty sub-sequences are parsed as {@link Double#NaN}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static double[] parseDoubles(final CharSequence values, final char separator)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final double[] parsed = new double[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            final String token = tokens[i].toString().trim();
            parsed[i] = token.isEmpty() ? Double.NaN : Double.parseDouble(token);
        }
        return parsed;
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Float#parseFloat(String) parses} each item as a {@code float}.
     * Empty sub-sequences are parsed as {@link Float#NaN}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static float[] parseFloats(final CharSequence values, final char separator)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final float[] parsed = new float[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            final String token = tokens[i].toString().trim();
            parsed[i] = token.isEmpty() ? Float.NaN : Float.parseFloat(token);
        }
        return parsed;
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Long#parseLong(String) parses} each item as a {@code long}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @param  radix     The radix to be used for parsing. This is usually 10.
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static long[] parseLongs(final CharSequence values, final char separator, final int radix)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final long[] parsed = new long[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            parsed[i] = Long.parseLong(tokens[i].toString().trim(), radix);
        }
        return parsed;
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Integer#parseInt(String) parses} each item as an {@code int}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @param  radix     The radix to be used for parsing. This is usually 10.
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static int[] parseInts(final CharSequence values, final char separator, final int radix)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final int[] parsed = new int[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            parsed[i] = Integer.parseInt(tokens[i].toString().trim(), radix);
        }
        return parsed;
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Short#parseShort(String) parses} each item as a {@code short}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @param  radix     The radix to be used for parsing. This is usually 10.
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static short[] parseShorts(final CharSequence values, final char separator, final int radix)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final short[] parsed = new short[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            parsed[i] = Short.parseShort(tokens[i].toString().trim(), radix);
        }
        return parsed;
    }

    /**
     * {@linkplain #split(CharSequence, char) Splits} the given text around the given character,
     * then {@linkplain Byte#parseByte(String) parses} each item as a {@code byte}.
     *
     * @param  values    The text containing the values to parse, or {@code null}.
     * @param  separator The delimiting character (typically the coma).
     * @param  radix     The radix to be used for parsing. This is usually 10.
     * @return The array of numbers parsed from the given string,
     *         or an empty array if {@code values} was null.
     * @throws NumberFormatException If at least one number can not be parsed.
     */
    public static byte[] parseBytes(final CharSequence values, final char separator, final int radix)
            throws NumberFormatException
    {
        final CharSequence[] tokens = split(values, separator);
        final byte[] parsed = new byte[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            parsed[i] = Byte.parseByte(tokens[i].toString().trim(), radix);
        }
        return parsed;
    }

    /**
     * Formats the given elements as a (typically) comma-separated list. This method is similar to
     * {@link java.util.AbstractCollection#toString()} or {@link java.util.Arrays#toString(Object[])}
     * except for the following:
     * <p>
     * <ul>
     *   <li>There is no leading {@code '['} or trailing {@code ']'} characters.</li>
     *   <li>Null elements are ignored instead than formatted as {@code "null"}.</li>
     *   <li>If the {@code collection} argument is null or contains only null elements,
     *       then this method returns {@code null}.</li>
     *   <li>In the common case where the collection contains a single {@link String} element,
     *       that string is returned directly (no object duplication).</li>
     * </ul>
     * <p>
     * This method is the converse of {@link #getLinesFromMultilines(CharSequence)}
     * when the separator is the system line separator.
     *
     * @param  collection The elements to format in a (typically) comma-separated list, or {@code null}.
     * @param  separator  The element separator, which is usually {@code ", "}.
     * @return The (typically) comma-separated list, or {@code null} if the given {@code collection}
     *         was null or contains only null elements.
     */
    public static String formatList(final Iterable<?> collection, final String separator) {
        ArgumentChecks.ensureNonNull("separator", separator);
        String list = null;
        if (collection != null) {
            StringBuilder buffer = null;
            for (final Object element : collection) {
                if (element != null) {
                    if (list == null) {
                        list = element.toString();
                    } else {
                        if (buffer == null) {
                            buffer = new StringBuilder(list);
                        }
                        buffer.append(separator).append(element);
                    }
                }
            }
            if (buffer != null) {
                list = buffer.toString();
            }
        }
        return list;
    }

    /**
     * Returns a text with leading and trailing white spaces omitted. White spaces are identified
     * by the {@link Character#isWhitespace(int)} method.
     * <p>
     * This method is similar in purpose to {@link String#trim()}, except that the later considers
     * every ASCII control codes below 32 to be a whitespace. This have the side effect of removing
     * {@linkplain org.apache.sis.io.X364 X3.64} escape sequences as well. Users should invoke this
     * {@code CharSequences.trimWhitespaces} method instead if they need to preserve X3.64 escape
     * sequences.
     *
     * @param  text The text from which to remove leading and trailing white spaces, or {@code null}.
     * @return A string with leading and trailing white spaces removed, or {@code null} is the given
     *         string was null.
     *
     * @see String#trim()
     */
    public static CharSequence trimWhitespaces(CharSequence text) {
        if (text != null) {
            int upper = text.length();
            while (upper != 0) {
                final int c = codePointBefore(text, upper);
                if (!isWhitespace(c)) break;
                upper -= charCount(c);
            }
            int lower = 0;
            while (lower < upper) {
                final int c = codePointAt(text, lower);
                if (!isWhitespace(c)) break;
                lower += charCount(c);
            }
            text = text.subSequence(lower, upper);
        }
        return text;
    }

    /**
     * Trims the fractional part of the given formatted number, provided that it doesn't change
     * the value. This method assumes that the number is formatted in the US locale, typically
     * by the {@link Double#toString(double)} method.
     * <p>
     * More specifically if the given value ends with a {@code '.'} character followed by a
     * sequence of {@code '0'} characters, then those characters are omitted. Otherwise this
     * method returns the string unchanged. This is a "<cite>all or nothing</cite>" method:
     * either the fractional part is completely removed, or either it is left unchanged.
     *
     * {@section Examples}
     * This method returns {@code "4"} if the given value is {@code "4."}, {@code "4.0"} or
     * {@code "4.00"}, but returns {@code "4.10"} unchanged (including the trailing {@code '0'}
     * character) if the input is {@code "4.10"}.
     *
     * {@section Use case}
     * This method is useful before to {@linkplain Integer#parseInt(String) parse a number}
     * if that number should preferably be parsed as an integer before attempting to parse
     * it as a floating point number.
     *
     * @param  value The value to trim if possible, or {@code null}.
     * @return The value without the trailing {@code ".0"} part (if any),
     *         or {@code null} if the given string was null.
     *
     * @see StringBuilders#trimFractionalPart(StringBuilder)
     */
    public static CharSequence trimFractionalPart(final CharSequence value) {
        if (value != null) {
            for (int i=value.length(); i>0;) {
                final int c = codePointBefore(value, i);
                i -= charCount(c);
                switch (c) {
                    case '0': continue;
                    case '.': return value.subSequence(0, i);
                    default : return value;
                }
            }
        }
        return value;
    }

    /**
     * Replaces some Unicode characters by ASCII characters on a "best effort basis".
     * For example the {@code 'é'} character is replaced by {@code 'e'} (without accent).
     * <p>
     * The current implementation replaces only the characters in the range {@code 00C0}
     * to {@code 00FF}, inclusive. Other characters are left unchanged.
     *
     * @param  text The text to scan for Unicode characters to replace by ASCII characters,
     *         or {@code null}.
     * @return The given text with substitution applied, or {@code text} if no replacement
     *         has been applied.
     *
     * @see StringBuilders#toASCII(StringBuilder)
     */
    public static CharSequence toASCII(final CharSequence text) {
        return StringBuilders.toASCII(text, null);
    }

    /**
     * Given a string in camel cases (typically a Java identifier), returns a string formatted
     * like an English sentence. This heuristic method performs the following steps:
     *
     * <ol>
     *   <li><p>Invoke {@link #camelCaseToWords(CharSequence, boolean)}, which separate the words
     *     on the basis of character case. For example {@code "transferFunctionType"} become
     *     "<cite>transfer function type</cite>". This works fine for ISO 19115 identifiers.</p></li>
     *
     *   <li><p>Next replace all occurrence of {@code '_'} by spaces in order to take in account
     *     an other common naming convention, which uses {@code '_'} as a word separator. This
     *     convention is used by NetCDF attributes like {@code "project_name"}.</p></li>
     *
     *   <li><p>Finally ensure that the first character is upper-case.</p></li>
     * </ol>
     *
     * {@section Exception to the above rules}
     * If the given identifier contains only upper-case letters, digits and the {@code '_'}
     * character, then the identifier is returned "as is" except for the {@code '_'} characters
     * which are replaced by {@code '-'}. This work well for identifiers like {@code "UTF-8"} or
     * {@code "ISO-LATIN-1"} for example.
     * <p>
     * Note that those heuristic rules may be modified in future SIS versions,
     * depending on the practical experience gained.
     *
     * @param  identifier An identifier with no space, words begin with an upper-case character,
     *         or {@code null}.
     * @return The identifier with spaces inserted after what looks like words, or {@code null}
     *         if the given {@code identifier} argument was null.
     */
    public static CharSequence camelCaseToSentence(final CharSequence identifier) {
        if (identifier == null) {
            return null;
        }
        final StringBuilder buffer;
        if (isCode(identifier)) {
            if (identifier instanceof String) {
                return ((String) identifier).replace('_', '-');
            }
            buffer = new StringBuilder(identifier);
            replace(buffer, '_', '-');
        } else {
            buffer = (StringBuilder) camelCaseToWords(identifier, true);
            final int length = buffer.length();
            if (length != 0) {
                replace(buffer, '_', ' ');
                final int c = buffer.codePointAt(0);
                final int up = toUpperCase(c);
                if (c != up) {
                    replace(buffer, 0, charCount(c), toChars(up));
                }
            }
        }
        return buffer;
    }

    /**
     * Given a string in camel cases, returns a string with the same words separated by spaces.
     * A word begins with a upper-case character following a lower-case character. For example
     * if the given string is {@code "PixelInterleavedSampleModel"}, then this method returns
     * "<cite>Pixel Interleaved Sample Model</cite>" or "<cite>Pixel interleaved sample model</cite>"
     * depending on the value of the {@code toLowerCase} argument.
     * <p>
     * If {@code toLowerCase} is {@code false}, then this method inserts spaces but does not change
     * the case of characters. If {@code toLowerCase} is {@code true}, then this method changes
     * {@linkplain Character#toLowerCase(int) to lower case} the first character after each spaces
     * inserted by this method (note that this intentionally exclude the very first character in
     * the given string), except if the second character {@linkplain Character#isUpperCase(int)
     * is upper case}, in which case the word is assumed an acronym.
     * <p>
     * The given string is usually a programmatic identifier like a class name or a method name.
     *
     * @param  identifier An identifier with no space, words begin with an upper-case character.
     * @param  toLowerCase {@code true} for changing the first character of words to lower case,
     *         except for the first word and acronyms.
     * @return The identifier with spaces inserted after what looks like words, or {@code null}
     *         if the given {@code identifier} argument was null.
     */
    public static CharSequence camelCaseToWords(final CharSequence identifier, final boolean toLowerCase) {
        if (identifier == null) {
            return null;
        }
        /*
         * Implementation note: the 'camelCaseToSentence' method needs
         * this method to unconditionally returns a new StringBuilder.
         */
        final int length = identifier.length();
        final StringBuilder buffer = new StringBuilder(length + 8);
        final int lastIndex = (length != 0) ? length - charCount(codePointBefore(identifier, length)) : 0;
        int last = 0;
        for (int i=1; i<=length;) {
            final int cp;
            final boolean doAppend;
            if (i == length) {
                cp = 0;
                doAppend = true;
            } else {
                cp = codePointAt(identifier, i);
                doAppend = Character.isUpperCase(cp) && isLowerCase(codePointBefore(identifier, i));
            }
            if (doAppend) {
                final int pos = buffer.length();
                buffer.append(identifier, last, i).append(' ');
                if (toLowerCase && pos!=0 && last<lastIndex && isLowerCase(codePointAfter(identifier, last))) {
                    final int c = buffer.codePointAt(pos);
                    final int low = toLowerCase(c);
                    if (c != low) {
                        replace(buffer, pos, pos + charCount(c), toChars(low));
                    }
                }
                last = i;
            }
            i += charCount(cp);
        }
        /*
         * Removes the trailing space, if any.
         */
        final int lg = buffer.length();
        if (lg != 0) {
            final int cp = buffer.codePointBefore(lg);
            if (isSpaceChar(cp)) {
                buffer.setLength(lg - charCount(cp));
            }
        }
        return buffer;
    }

    /**
     * Creates an acronym from the given text. If every characters in the given text are upper
     * case, then the text is returned unchanged on the assumption that it is already an acronym.
     * Otherwise this method returns a string containing the first character of each word, where
     * the words are separated by the camel case convention, the {@code '_'} character, or any
     * character which is not a {@linkplain Character#isJavaIdentifierPart(int) java identifier
     * part} (including spaces).
     * <p>
     * <b>Examples:</b> given {@code "northEast"}, this method returns {@code "NE"}.
     * Given {@code "Open Geospatial Consortium"}, this method returns {@code "OGC"}.
     *
     * @param  text The text for which to create an acronym, or {@code null}.
     * @return The acronym, or {@code null} if the given text was null.
     */
    public static CharSequence camelCaseToAcronym(CharSequence text) {
        if (text != null && !isUpperCase(text = trimWhitespaces(text))) {
            final int length = text.length();
            final StringBuilder buffer = new StringBuilder(8); // Acronyms are usually short.
            boolean wantChar = true;
            for (int i=0; i<length;) {
                final int c = codePointAt(text, i);
                if (wantChar) {
                    if (isJavaIdentifierStart(c)) {
                        buffer.appendCodePoint(c);
                        wantChar = false;
                    }
                } else if (!isJavaIdentifierPart(c) || c == '_') {
                    wantChar = true;
                } else if (Character.isUpperCase(c)) {
                    // Test for mixed-case (e.g. "northEast").
                    // Note that the buffer is guaranteed to contain at least 1 character.
                    if (isLowerCase(buffer.codePointBefore(buffer.length()))) {
                        buffer.appendCodePoint(c);
                    }
                }
                i += charCount(c);
            }
            final int acrlg = buffer.length();
            if (acrlg != 0) {
                /*
                 * If every characters except the first one are upper-case, ensure that the
                 * first one is upper-case as well. This is for handling the identifiers which
                 * are compliant to Java-Beans convention (e.g. "northEast").
                 */
                if (isUpperCase(buffer, 1, acrlg)) {
                    final int c = buffer.codePointAt(0);
                    final int up = toUpperCase(c);
                    if (c != up) {
                        replace(buffer, 0, charCount(c), toChars(up));
                    }
                }
                if (!equals(text, buffer)) {
                    text = buffer;
                }
            }
        }
        return text;
    }

    /**
     * Returns {@code true} if the first string is likely to be an acronym of the second string.
     * An acronym is a sequence of {@linkplain Character#isLetterOrDigit(int) letters or digits}
     * built from at least one character of each word in the {@code words} string. More than
     * one character from the same word may appear in the acronym, but they must always
     * be the first consecutive characters. The comparison is case-insensitive.
     * <p>
     * <b>Example:</b> given the string {@code "Open Geospatial Consortium"}, the following
     * strings are recognized as acronyms: {@code "OGC"}, {@code "ogc"}, {@code "O.G.C."},
     * {@code "OpGeoCon"}.
     *
     * @param  acronym A possible acronym of the sequence of words.
     * @param  words The sequence of words.
     * @return {@code true} if the first string is an acronym of the second one.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static boolean isAcronymForWords(final CharSequence acronym, final CharSequence words) {
        final int lgc = words.length();
        final int lga = acronym.length();
        int ic=0, ia=0;
        int ca, cc;
        do {
            if (ia >= lga) return false;
            ca = codePointAt(acronym, ia);
            ia += charCount(ca);
        } while (!isLetterOrDigit(ca));
        do {
            if (ic >= lgc) return false;
            cc = codePointAt(words, ic);
            ic += charCount(cc);
        }
        while (!isLetterOrDigit(cc));
        if (toUpperCase(ca) != toUpperCase(cc)) {
            // The first letter must match.
            return false;
        }
cmp:    while (ia < lga) {
            if (ic >= lgc) {
                // There is more letters in the acronym than in the complete name.
                return false;
            }
            ca = codePointAt(acronym, ia); ia += charCount(ca);
            cc = codePointAt(words,   ic); ic += charCount(cc);
            if (isLetterOrDigit(ca)) {
                if (toUpperCase(ca) == toUpperCase(cc)) {
                    // Acronym letter matches the letter from the complete name.
                    // Continue the comparison with next letter of both strings.
                    continue;
                }
                // Will search for the next word after the 'else' block.
            } else do {
                if (ia >= lga) break cmp;
                ca = codePointAt(acronym, ia);
                ia += charCount(ca);
            } while (!isLetterOrDigit(ca));
            /*
             * At this point, 'ca' is the next acronym letter to compare and we
             * need to search for the next word in the complete name. We first
             * skip remaining letters, then we skip non-letter characters.
             */
            boolean skipLetters = true;
            do while (isLetterOrDigit(cc) == skipLetters) {
                if (ic >= lgc) {
                    return false;
                }
                cc = codePointAt(words, ic);
                ic += charCount(cc);
            } while ((skipLetters = !skipLetters) == false);
            // Now that we are aligned on a new word, the first letter must match.
            if (toUpperCase(ca) != toUpperCase(cc)) {
                return false;
            }
        }
        /*
         * Now that we have processed all acronym letters, the complete name can not have
         * any additional word. We can only finish the current word and skip trailing non-
         * letter characters.
         */
        boolean skipLetters = true;
        do {
            do {
                if (ic >= lgc) return true;
                cc = codePointAt(words, ic);
                ic += charCount(cc);
            } while (isLetterOrDigit(cc) == skipLetters);
        } while ((skipLetters = !skipLetters) == false);
        return false;
    }

    /**
     * Returns {@code true} if the given string contains only upper case letters or digits.
     * A few punctuation characters like {@code '_'} and {@code '.'} are also accepted.
     * <p>
     * This method is used for identifying character strings that are likely to be code
     * like {@code "UTF-8"} or {@code "ISO-LATIN-1"}.
     *
     * @see #isJavaIdentifier(CharSequence)
     */
    private static boolean isCode(final CharSequence identifier) {
        for (int i=identifier.length(); --i>=0;) {
            final char c = identifier.charAt(i);
            // No need to use the code point API here, since the conditions
            // below are requiring the characters to be in the basic plane.
            if (!((c >= 'A' && c <= 'Z') || (c >= '-' && c <= ':') || c == '_')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the given identifier is a legal Java identifier.
     * This method returns {@code true} if the identifier length is greater than zero,
     * the first character is a {@linkplain Character#isJavaIdentifierStart(int) Java
     * identifier start} and all remaining characters (if any) are
     * {@linkplain Character#isJavaIdentifierPart(int) Java identifier parts}.
     *
     * @param identifier The character sequence to test.
     * @return {@code true} if the given character sequence is a legal Java identifier.
     * @throws NullPointerException if the argument is null.
     */
    public static boolean isJavaIdentifier(final CharSequence identifier) {
        final int length = identifier.length();
        if (length == 0) {
            return false;
        }
        int c = codePointAt(identifier, 0);
        if (!isJavaIdentifierStart(c)) {
            return false;
        }
        for (int i=0; (i += charCount(c)) < length;) {
            c = codePointAt(identifier, i);
            if (!isJavaIdentifierPart(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if every characters in the given character sequence are
     * {@linkplain Character#isUpperCase(int) upper-case}.
     *
     * @param  text The character sequence to test.
     * @return {@code true} if every character are upper-case.
     * @throws NullPointerException if the argument is null.
     *
     * @see String#toUpperCase()
     */
    public static boolean isUpperCase(final CharSequence text) {
        return isUpperCase(text, 0, text.length());
    }

    /**
     * Same as {@link #isUpperCase(CharSequence)}, but on a sub-sequence.
     */
    private static boolean isUpperCase(final CharSequence text, int lower, final int upper) {
        while (lower < upper) {
            final int c = codePointAt(text, lower);
            if (!Character.isUpperCase(c)) {
                return false;
            }
            lower += charCount(c);
        }
        return true;
    }

    /**
     * Returns {@code true} if the given code points are equal, ignoring case.
     * This method implements the same comparison algorithm than String#equalsIgnoreCase(String).
     * <p>
     * This method does not verify if {@code c1 == c2}. This check should have been done
     * by the caller, since the caller code is a more optimal place for this check.
     */
    private static boolean equalsIgnoreCase(int c1, int c2) {
        c1 = toUpperCase(c1);
        c2 = toUpperCase(c2);
        if (c1 == c2) {
            return true;
        }
        // Need this check for Georgian alphabet.
        return toLowerCase(c1) == toLowerCase(c2);
    }

    /**
     * Returns {@code true} if the two given texts are equal, ignoring case.
     * This method is similar to {@link String#equalsIgnoreCase(String)}, except
     * it works on arbitrary character sequences and compares <cite>code points</cite>
     * instead than characters.
     *
     * @param  s1 The first string to compare, or {@code null}.
     * @param  s2 The second string to compare, or {@code null}.
     * @return {@code true} if the two given texts are equal, ignoring case.
     *
     * @see String#equalsIgnoreCase(String)
     */
    public static boolean equalsIgnoreCase(final CharSequence s1, final CharSequence s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        final int lg1 = s1.length();
        final int lg2 = s2.length();
        int i1 = 0, i2 = 0;
        while (i1<lg1 && i2<lg2) {
            final int c1 = codePointAt(s1, i1);
            final int c2 = codePointAt(s2, i2);
            if (c1 != c2 && !equalsIgnoreCase(c1, c2)) {
                return false;
            }
            i1 += charCount(c1);
            i2 += charCount(c2);
        }
        return i1 == i2;
    }

    /**
     * Returns {@code true} if the two given texts are equal. This method delegates to
     * {@link String#contentEquals(CharSequence)} if possible. This method never invoke
     * {@link CharSequence#toString()} in order to avoid a potentially large copy of data.
     *
     * @param  s1 The first string to compare, or {@code null}.
     * @param  s2 The second string to compare, or {@code null}.
     * @return {@code true} if the two given texts are equal.
     *
     * @see String#contentEquals(CharSequence)
     */
    public static boolean equals(final CharSequence s1, final CharSequence s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 != null && s2 != null) {
            if (s1 instanceof String) return ((String) s1).contentEquals(s2);
            if (s2 instanceof String) return ((String) s2).contentEquals(s1);
            final int length = s1.length();
            if (s2.length() == length) {
                for (int i=0; i<length; i++) {
                    if (s1.charAt(i) != s2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given string at the given offset contains the given part,
     * in a case-sensitive comparison. This method is equivalent to the following code:
     *
     * {@preformat java
     *     return string.regionMatches(offset, part, 0, part.length());
     * }
     *
     * Except that this method works on arbitrary {@link CharSequence} objects instead than
     * {@link String}s only.
     *
     * @param string The string for which to tests for the presence of {@code part}.
     * @param offset The offset in {@code string} where to test for the presence of {@code part}.
     * @param part   The part which may be present in {@code string}.
     * @return {@code true} if {@code string} contains {@code part} at the given {@code offset}.
     * @throws NullPointerException if any of the arguments is null.
     *
     * @see String#regionMatches(int, String, int, int)
     */
    public static boolean regionMatches(final CharSequence string, final int offset, final CharSequence part) {
        if (string instanceof String && part instanceof String) {
            return ((String) string).regionMatches(offset, (String) part, 0, part.length());
        }
        final int length = part.length();
        if (offset + length > string.length()) {
            return false;
        }
        for (int i=0; i<length; i++) {
            // No need to use the code point API here, since we are looking for exact matches.
            if (string.charAt(offset + i) != part.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the index within the given character sequence of the first occurrence of the
     * specified character, starting the search at the specified index. If the character is
     * not found, then this method returns -1.
     * <p>
     * There is no restriction on the value of {@code fromIndex}. If negative or greater
     * than the length of the text, then the behavior of this method is the same than the
     * one documented in {@link String#indexOf(int, int)}.
     *
     * @param  text      The character sequence in which to perform the search, or {@code null}.
     * @param  toSearch  The Unicode code point of the character to search.
     * @param  fromIndex The index to start the search from.
     * @return The index of the first occurrence of the given character in the text, or -1
     *         if no occurrence has been found or if the {@code text} argument is null.
     *
     * @see String#indexOf(int, int)
     */
    public static int indexOf(final CharSequence text, final int toSearch, int fromIndex) {
        if (text != null) {
            if (text instanceof String) {
                // String provides a faster implementation.
                return ((String) text).indexOf(toSearch, fromIndex);
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            final int length = text.length();
            while (fromIndex < length) {
                final int c = codePointAt(text, fromIndex);
                if (c == toSearch) {
                    return fromIndex;
                }
                fromIndex += charCount(c);
            }
        }
        return -1;
    }

    /**
     * Returns the index within the given strings of the first occurrence of the specified part,
     * starting at the specified index. This method is equivalent to the following code:
     *
     * {@preformat java
     *     return string.indexOf(part, fromIndex);
     * }
     *
     * Except that this method works on arbitrary {@link CharSequence} objects instead than
     * {@link String}s only.
     *
     * @param  string    The string in which to perform the search.
     * @param  part      The substring for which to search.
     * @param  fromIndex The index from which to start the search.
     * @return The index within the string of the first occurrence of the specified part,
     *         starting at the specified index, or -1 if none.
     * @throws NullPointerException if any of the arguments is null.
     *
     * @see String#indexOf(String, int)
     * @see StringBuilder#indexOf(String, int)
     * @see StringBuffer#indexOf(String, int)
     */
    public static int indexOf(final CharSequence string, final CharSequence part, int fromIndex) {
        if (string != null) {
            if (part instanceof String) {
                if (string instanceof String) {
                    return ((String) string).indexOf((String) part, fromIndex);
                }
                if (string instanceof StringBuilder) {
                    return ((StringBuilder) string).indexOf((String) part, fromIndex);
                }
                if (string instanceof StringBuffer) {
                    return ((StringBuffer) string).indexOf((String) part, fromIndex);
                }
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            final int length = part.length();
            final int stopAt = string.length() - length;
search:     for (; fromIndex <= stopAt; fromIndex++) {
                for (int i=0; i<length; i++) {
                    // No need to use the codePointAt API here, since we are looking for exact matches.
                    if (string.charAt(fromIndex + i) != part.charAt(i)) {
                        continue search;
                    }
                }
                return fromIndex;
            }
        }
        return -1;
    }

    /**
     * Returns the token starting at the given offset in the given text. For the purpose of this
     * method, a "token" is any sequence of consecutive characters of the same type, as defined
     * below.
     * <p>
     * Let define <var>c</var> as the first non-blank character located at an index equals or
     * greater than the given offset. Then the characters that are considered of the same type
     * are:
     * <p>
     * <ul>
     *   <li>If <var>c</var> is a
     *       {@linkplain Character#isJavaIdentifierStart(int) Java identifier start},
     *       then any following character that are
     *       {@linkplain Character#isJavaIdentifierPart(int) Java identifier part}.</li>
     *   <li>Otherwise any character for which {@link Character#getType(int)} returns
     *       the same value than for <var>c</var>.</li>
     * </ul>
     *
     * @param  text The text for which to get the token.
     * @param  offset Index of the fist character to consider in the given text.
     * @return A sub-sequence of {@code text} starting at the given offset, or an empty string
     *         if there is no non-blank character at or after the given offset.
     * @throws NullPointerException if the {@code text} argument is null.
     */
    public static CharSequence token(final CharSequence text, int offset) {
        final int length = text.length();
        int upper = offset;
        /*
         * Skip whitespaces. At the end of this loop,
         * 'c' will be the first non-blank character.
         */
        int c;
        do {
            if (upper >= length) return "";
            c = codePointAt(text, upper);
            offset = upper;
            upper += charCount(c);
        }
        while (isWhitespace(c));
        /*
         * Advance over all characters "of the same type".
         */
        if (isJavaIdentifierStart(c)) {
            while (upper<length && isJavaIdentifierPart(c = codePointAt(text, upper))) {
                upper += charCount(c);
            }
        } else {
            final int type = getType(codePointAt(text, offset));
            while (upper<length && getType(c = codePointAt(text, upper)) == type) {
                upper += charCount(c);
            }
        }
        return text.subSequence(offset, upper);
    }

    /**
     * Returns the longest sequence of characters which is found at the beginning of the two
     * given texts. If one of those texts is {@code null}, then the other text is returned.
     *
     * @param  s1 The first text,  or {@code null}.
     * @param  s2 The second text, or {@code null}.
     * @return The common prefix of both texts, or {@code null} if both texts are null.
     */
    public static CharSequence commonPrefix(final CharSequence s1, final CharSequence s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        final CharSequence shortest;
        final int lg1 = s1.length();
        final int lg2 = s2.length();
        final int length;
        if (lg1 <= lg2) {
            shortest = s1;
            length = lg1;
        } else {
            shortest = s2;
            length = lg2;
        }
        int i = 0;
        while (i < length) {
            // No need to use the codePointAt API here, since we are looking for exact matches.
            if (s1.charAt(i) != s2.charAt(i)) {
                break;
            }
            i++;
        }
        return shortest.subSequence(0, i);
    }

    /**
     * Returns the longest sequence of characters which is found at the end of the two given texts.
     * If one of those texts is {@code null}, then the other text is returned.
     *
     * @param  s1 The first text,  or {@code null}.
     * @param  s2 The second text, or {@code null}.
     * @return The common suffix of both texts, or {@code null} if both texts are null.
     */
    public static CharSequence commonSuffix(final CharSequence s1, final CharSequence s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        final CharSequence shortest;
        final int lg1 = s1.length();
        final int lg2 = s2.length();
        final int length;
        if (lg1 <= lg2) {
            shortest = s1;
            length = lg1;
        } else {
            shortest = s2;
            length = lg2;
        }
        int i = 0;
        while (++i <= length) {
            // No need to use the codePointAt API here, since we are looking for exact matches.
            if (s1.charAt(lg1 - i) != s2.charAt(lg2 - i)) {
                break;
            }
        }
        i--;
        return shortest.subSequence(length - i, shortest.length());
    }

    /**
     * Returns {@code true} if the given character sequence starts with the given prefix.
     *
     * @param  sequence    The sequence to test.
     * @param  prefix      The expected prefix.
     * @param  ignoreCase  {@code true} if the case should be ignored.
     * @return {@code true} if the given sequence starts with the given prefix.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static boolean startsWith(final CharSequence sequence, final CharSequence prefix, final boolean ignoreCase) {
        final int lgs = sequence.length();
        final int lgp = prefix  .length();
        int is = 0;
        int ip = 0;
        while (ip < lgp) {
            if (is >= lgs) {
                return false;
            }
            final int cs = codePointAt(sequence, is);
            final int cp = codePointAt(prefix,   ip);
            if (cs != cp && (!ignoreCase || !equalsIgnoreCase(cs, cp))) {
                return false;
            }
            is += charCount(cs);
            ip += charCount(cp);
        }
        return true;
    }

    /**
     * Returns {@code true} if the given character sequence ends with the given suffix.
     *
     * @param  sequence    The sequence to test.
     * @param  suffix      The expected suffix.
     * @param  ignoreCase  {@code true} if the case should be ignored.
     * @return {@code true} if the given sequence ends with the given suffix.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static boolean endsWith(final CharSequence sequence, final CharSequence suffix, final boolean ignoreCase) {
        int is = sequence.length();
        int ip = suffix  .length();
        while (ip > 0) {
            if (is <= 0) {
                return false;
            }
            final int cs = codePointBefore(sequence, is);
            final int cp = codePointBefore(suffix,   ip);
            if (cs != cp && (!ignoreCase || !equalsIgnoreCase(cs, cp))) {
                return false;
            }
            is -= charCount(cs);
            ip -= charCount(cp);
        }
        return true;
    }

    /**
     * Returns the index of the first character after the given number of lines.
     * This method counts the number of occurrence of {@code '\n'}, {@code '\r'}
     * or {@code "\r\n"} starting from the given position. When {@code numToSkip}
     * occurrences have been found, the index of the first character after the last
     * occurrence is returned.
     *
     * @param  string    The string in which to skip a determined amount of lines.
     * @param  numToSkip The number of lines to skip. Can be positive, zero or negative.
     * @param  startAt   Index at which to start the search.
     * @return Index of the first character after the last skipped line.
     * @throws NullPointerException if the {@code string} argument is null.
     */
    public static int skipLines(final CharSequence string, int numToSkip, int startAt) {
        final int length = string.length();
        /*
         * Go backward if the number of lines is negative.
         * No need to use the codePoint API because we are
         * looking only for '\r' and '\n' characters.
         */
        if (numToSkip < 0) {
            do {
                char c;
                do {
                    if (startAt == 0) {
                        return startAt;
                    }
                    c = string.charAt(--startAt);
                    if (c == '\n') {
                        if (startAt != 0 && string.charAt(startAt - 1) == '\r') {
                            --startAt;
                        }
                        break;
                    }
                } while (c != '\r');
            } while (++numToSkip != 0);
            numToSkip = 1; // For skipping the "end of line" characters.
        }
        /*
         * Skips forward the given amount of lines.
         */
        while (--numToSkip >= 0) {
            char c;
            do {
                if (startAt >= length) {
                    return startAt;
                }
                c = string.charAt(startAt++);
                if (c == '\r') {
                    if (startAt != length && string.charAt(startAt) == '\n') {
                        startAt++;
                    }
                    break;
                }
            } while (c != '\n');
        }
        return startAt;
    }

    /**
     * Returns a {@link CharSequence} instance for each line found in a multi-lines text.
     * Each element in the returned array will be a single line. If the given text is already
     * a single line, then this method returns a singleton containing only the given text.
     * <p>
     * The converse of this method is {@link #formatList(Iterable, String)}.
     *
     * {@note Prior JDK8 this method was relatively cheap because all string instances created by
     * <code>String.substring(int,int)</code> shared the same <code>char[]</code> internal array.
     * However since JDK8, the new <code>String</code> implementation copies the data in new arrays.
     * Consequently it is better to use index rather than this method for splitting large
     * <code>String</code>s. However this method still useful for other <code>CharSequence</code>
     * implementations providing an efficient <code>subSequence(int,int)</code> method.}
     *
     * @param  text The multi-line text from which to get the individual lines.
     * @return The lines in the text, or {@code null} if the given text was null.
     */
    public static CharSequence[] getLinesFromMultilines(final CharSequence text) {
        if (text == null) {
            return null;
        }
        /*
         * This method is implemented on top of String.indexOf(int,int), which is the
         * fatest method available while taking care of the complexity of code points.
         */
        int lf = indexOf(text, '\n', 0);
        int cr = indexOf(text, '\r', 0);
        if (lf < 0 && cr < 0) {
            return new CharSequence[] {
                text
            };
        }
        int count = 0;
        CharSequence[] splitted = new CharSequence[8];
        int last = 0;
        boolean hasMore;
        do {
            int skip = 1;
            final int splitAt;
            if (cr < 0) {
                // There is no "\r" character in the whole text, only "\n".
                splitAt = lf;
                hasMore = (lf = indexOf(text, '\n', lf+1)) >= 0;
            } else if (lf < 0) {
                // There is no "\n" character in the whole text, only "\r".
                splitAt = cr;
                hasMore = (cr = indexOf(text, '\r', cr+1)) >= 0;
            } else if (lf < cr) {
                // There is both "\n" and "\r" characters with "\n" first.
                splitAt = lf;
                hasMore = true;
                lf = indexOf(text, '\n', lf+1);
            } else {
                // There is both "\r" and "\n" characters with "\r" first.
                // We need special care for the "\r\n" sequence.
                splitAt = cr;
                if (lf == ++cr) {
                    cr = indexOf(text, '\r', cr+1);
                    lf = indexOf(text, '\n', lf+1);
                    hasMore = (cr >= 0 || lf >= 0);
                    skip = 2;
                } else {
                    cr = indexOf(text, '\r', cr+1);
                    hasMore = true; // Because there is lf.
                }
            }
            if (count >= splitted.length) {
                splitted = copyOf(splitted, count*2);
            }
            splitted[count++] = text.subSequence(last, splitAt);
            last = splitAt + skip;
        } while (hasMore);
        /*
         * Add the remaining string and we are done.
         */
        if (count >= splitted.length) {
            splitted = copyOf(splitted, count+1);
        }
        splitted[count++] = text.subSequence(last, text.length());
        return resize(splitted, count);
    }
}