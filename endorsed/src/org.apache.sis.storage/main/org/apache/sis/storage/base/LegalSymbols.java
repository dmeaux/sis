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
package org.apache.sis.storage.base;

import java.time.LocalDate;
import java.util.Date;
import java.util.Collections;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Restriction;
import org.apache.sis.util.CharSequences;
import org.apache.sis.metadata.iso.citation.AbstractParty;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import static org.apache.sis.util.internal.StandardDateFormat.MILLISECONDS_PER_DAY;

// Specific to the main and geoapi-3.1 branches:
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;


/**
 * Elements to omit in the legal notice to be parsed by {@link MetadataBuilder#parseLegalNotice(String)}.
 * Some of those elements are implied by the metadata were the legal notice will be stored.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class LegalSymbols {
    /**
     * Symbols associated to restrictions.
     */
    private static final LegalSymbols[] VALUES = {
        new LegalSymbols(Restriction.COPYRIGHT, "COPYRIGHT", "(C)", "©", "All rights reserved"),
        new LegalSymbols(Restriction.TRADEMARK, "TRADEMARK", "(TM)", "™", "(R)", "®")
    };

    /**
     * The restriction to use if an item in the {@linkplain #symbols} list is found.
     */
    private final Restriction restriction;

    /**
     * Symbols to use as an indication that the {@linkplain #restriction} applies.
     */
    private final String[] symbols;

    /**
     * Creates a new enumeration value for the given symbol.
     */
    private LegalSymbols(final Restriction restriction, final String... symbols) {
        this.restriction = restriction;
        this.symbols = symbols;
    }

    /**
     * Returns {@code true} if the given character is a space or a punctuation of category "other".
     * The punctuation characters include coma, dot, semi-colon, <i>etc.</i> but do not include
     * parenthesis or connecting punctuation.
     *
     * @param c the Unicode code point of the character to test.
     */
    private static boolean isSpaceOrPunctuation(final int c) {
        switch (Character.getType(c)) {
            case Character.LINE_SEPARATOR:
            case Character.SPACE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.OTHER_PUNCTUATION: return true;
            default: return false;
        }
    }

    /**
     * Implementation of {@link MetadataBuilder#parseLegalNotice(String)}, provided here for reducing
     * the number of class loading in the common case where there is no legal notice to parse.
     */
    static void parse(final String notice, final DefaultLegalConstraints constraints) {
        final int length = notice.length();
        final var buffer = new StringBuilder(length);
        int     year           = 0;         // The copyright year, or 0 if none.
        int     quoteLevel     = 0;         // Incremented on ( [ « characters, decremented on ) ] » characters.
        boolean isCopyright    = false;     // Whether the word parsed by previous iteration was "Copyright" or "(C)".
        boolean wasSeparator   = true;      // Whether the caracter parsed by the previous iteration was a word separator.
        boolean wasPunctuation = true;      // Whether the previous character was a punctuation of Unicode category "other".
        boolean skipNextChars  = true;      // Whether the next spaces and some punction characters should be ignored.
parse:  for (int i = 0; i < length;) {
            final int c = notice.codePointAt(i);
            final int n = Character.charCount(c);
            int     quoteChange   = 0;
            boolean isSeparator   = false;
            boolean isPunctuation;
            switch (Character.getType(c)) {
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.START_PUNCTUATION: {
                    quoteChange   = +1;                     //  ( [ «  etc.
                    skipNextChars = false;
                    isPunctuation = false;
                    break;
                }
                case Character.FINAL_QUOTE_PUNCTUATION:
                case Character.END_PUNCTUATION: {
                    quoteChange   = -1;                     //  ) ] »  etc.
                    skipNextChars = false;
                    isPunctuation = false;
                    break;
                }
                default: {                                  // Letter, digit, hyphen, etc.
                    skipNextChars = false;
                    isPunctuation = false;
                    break;
                }
                case Character.OTHER_PUNCTUATION: {         //  , . : ; / " etc. but not -.
                    isPunctuation = true;
                    isSeparator   = true;
                    break;
                }
                case Character.LINE_SEPARATOR:
                case Character.SPACE_SEPARATOR:
                case Character.PARAGRAPH_SEPARATOR: {
                    isPunctuation = wasPunctuation;
                    isSeparator   = true;
                    break;
                }
            }
            if (wasSeparator && !isSeparator && quoteLevel == 0) {
                /*
                 * Found the beginning of a new word. Ignore textes like "(C)" or "All rights reserved".
                 * Some of those textes are implied by the metadata where the legal notice will be stored.
                 */
                for (final LegalSymbols r : VALUES) {
                    for (final String symbol : r.symbols) {
                        if (notice.regionMatches(true, i, symbol, 0, symbol.length())) {
                            final int after = i + symbol.length();
                            if (after >= length || isSpaceOrPunctuation(notice.codePointAt(after))) {
                                isCopyright |= (r.restriction == Restriction.COPYRIGHT);
                                constraints.getUseConstraints().add(r.restriction);
                                wasPunctuation = true;      // Pretend that "Copyright" was followed by a coma.
                                skipNextChars  = true;      // Ignore spaces and punctuations until the next word.
                                i = after;                  // Skip the "Copyright" (or other) word.
                                continue parse;
                            }
                        }
                    }
                }
                /*
                 * If a copyright notice is followed by digits, assume that those digits are the copyright year.
                 * We require the year is followed by punctuations or non-breaking space in order to reduce the
                 * risk of confusion with postal addresses. So this block should accept "John, 1992." but not
                 * "1992-1 Nowhere road".
                 */
                if (isCopyright && wasPunctuation && year == 0 && c >= '0' && c <= '9') {
                    int endOfDigits = i + n;            // After the last digit in sequence.
                    while (endOfDigits < length) {
                        final int d = notice.codePointAt(endOfDigits);
                        if (d < '0' || d > '9') break;
                        endOfDigits++;              // No need to use Character.charCount(s) here.
                    }
                    // Verify if the digits are followed by a punctuation.
                    final int endOfToken = CharSequences.skipLeadingWhitespaces(notice, endOfDigits, length);
                    if (endOfToken > endOfDigits || isSpaceOrPunctuation(notice.codePointAt(endOfToken))) try {
                        year = Integer.parseInt(notice.substring(i, endOfDigits));
                        if (year >= 1800 && year <= 9999) {                     // Those limits are arbitrary.
                            skipNextChars = true;
                            i = endOfToken;
                            continue;
                        }
                        year = 0;                                               // Reject as not a copyright year.
                    } catch (NumberFormatException e) {
                        // Not an integer - ignore, will be handled as text.
                    }
                }
            }
            /*
             * End of the block that was executed at the beginning of each new word.
             * Following is executed for every characters, except if the above block
             * skipped a portion of the input string.
             */
            wasPunctuation = isPunctuation;
            wasSeparator   = isSeparator;
            quoteLevel    += quoteChange;
            if (!skipNextChars && !Character.isIdentifierIgnorable(c)) {
                buffer.appendCodePoint(c);
            }
            i += n;
        }
        /*
         * End of parsing. Omit trailing spaces and some punctuations if any, then store the result.
         */
        int i = buffer.length();
        while (i > 0) {
            final int c = buffer.codePointBefore(i);
            if (!isSpaceOrPunctuation(c)) break;
            i -= Character.charCount(c);
        }
        final var c = new DefaultCitation(notice);
        if (year != 0) {
            final Date date = new Date(LocalDate.of(year, 1, 1).toEpochDay() * MILLISECONDS_PER_DAY);
            c.setDates(Collections.singleton(new DefaultCitationDate(date, DateType.valueOf("IN_FORCE"))));
        }
        if (i != 0) {
            buffer.setLength(i);
            // Same limitation as MetadataBuilder.party().
            final var party = new AbstractParty(buffer, null);
            final var r = new DefaultResponsibleParty(Role.OWNER);
            r.setParties(Collections.singleton(party));
            c.setCitedResponsibleParties(Collections.singleton(r));
        }
        constraints.getReferences().add(c);
    }
}
