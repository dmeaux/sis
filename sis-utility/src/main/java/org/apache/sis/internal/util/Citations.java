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
package org.apache.sis.internal.util;

import java.util.Collection;
import java.util.Iterator;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.apache.sis.util.Static;


/**
 * Utility methods working on {@link Citation} objects. The public facade of those methods is
 * defined in the {@link org.apache.sis.metadata.iso.citation.Citations} class, but the actual
 * implementation is defined here since it is needed by some utility methods.
 *
 * {@section Argument checks}
 * Every methods in this class accept {@code null} argument. This is different from the methods
 * in the {@link org.apache.sis.metadata.iso.citation.Citations} facade, which perform checks
 * against null argument for trapping user errors.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.2)
 * @version 0.3
 * @module
 */
public final class Citations extends Static {
    /**
     * Do not allows instantiation of this class.
     */
    private Citations() {
    }

    /**
     * Returns the collection iterator, or {@code null} if the given collection is null
     * or empty. We use this method as a paranoiac safety against broken implementations.
     *
     * @param  <E> The type of elements in the collection.
     * @param  collection The collection from which to get the iterator, or {@code null}.
     * @return The iterator over the given collection elements, or {@code null}.
     */
    public static <E> Iterator<E> iterator(final Collection<E> collection) {
        return (collection != null && !collection.isEmpty()) ? collection.iterator() : null;
    }

    /**
     * Returns {@code true} if at least one {@linkplain Citation#getTitle() title} or
     * {@linkplain Citation#getAlternateTitles alternate title} in {@code c1} is equal
     * to a title or alternate title in {@code c2}. The comparison is case-insensitive
     * and ignores leading and trailing spaces. The titles ordering is not significant.
     *
     * @param  c1 The first citation to compare, or {@code null}.
     * @param  c2 the second citation to compare, or {@code null}.
     * @return {@code true} if both arguments are non-null, and at least one title or
     *         alternate title matches.
     */
    public static boolean titleMatches(final Citation c1, final Citation c2) {
        if (c1 != null && c2 != null) {
            if (c1 == c2) {
                return true; // Optimisation for a common case.
            }
            InternationalString candidate = c2.getTitle();
            Iterator<? extends InternationalString> iterator = null;
            do {
                if (candidate != null) {
                    // The "null" locale argument is required for getting the unlocalized version.
                    final String asString = candidate.toString(null);
                    if (titleMatches(c1, asString)) {
                        return true;
                    }
                    final String asLocalized = candidate.toString();
                    if (asLocalized != asString // Slight optimization for a common case.
                            && titleMatches(c1, asLocalized))
                    {
                        return true;
                    }
                }
                if (iterator == null) {
                    iterator = iterator(c2.getAlternateTitles());
                    if (iterator == null) break;
                }
                if (!iterator.hasNext()) break;
                candidate = iterator.next();
            } while (true);
        }
        return false;
    }

    /**
     * Returns {@code true} if the {@linkplain Citation#getTitle() title} or any
     * {@linkplain Citation#getAlternateTitles alternate title} in the given citation
     * matches the given string. The comparison is case-insensitive and ignores leading
     * and trailing spaces.
     *
     * @param  citation The citation to check for, or {@code null}.
     * @param  title The title or alternate title to compare, or {@code null}.
     * @return {@code true} if both arguments are non-null, and the title or alternate
     *         title matches the given string.
     */
    public static boolean titleMatches(final Citation citation, String title) {
        if (citation != null && title != null) {
            title = title.trim();
            InternationalString candidate = citation.getTitle();
            Iterator<? extends InternationalString> iterator = null;
            do {
                if (candidate != null) {
                    // The "null" locale argument is required for getting the unlocalized version.
                    final String asString = candidate.toString(null);
                    if (asString != null && asString.trim().equalsIgnoreCase(title)) {
                        return true;
                    }
                    final String asLocalized = candidate.toString();
                    if (asLocalized != asString // Slight optimization for a common case.
                            && asLocalized != null && asLocalized.trim().equalsIgnoreCase(title))
                    {
                        return true;
                    }
                }
                if (iterator == null) {
                    iterator = iterator(citation.getAlternateTitles());
                    if (iterator == null) break;
                }
                if (!iterator.hasNext()) break;
                candidate = iterator.next();
            } while (true);
        }
        return false;
    }

    /**
     * Returns {@code true} if at least one {@linkplain Citation#getIdentifiers() identifier} in
     * {@code c1} is equal to an identifier in {@code c2}. The comparison is case-insensitive
     * and ignores leading and trailing spaces. The identifier ordering is not significant.
     *
     * <p>If (and <em>only</em> if) the citations do not contains any identifier, then this method
     * fallback on titles comparison using the {@link #titleMatches(Citation,Citation) titleMatches}
     * method. This fallback exists for compatibility with client codes using the citation
     * {@linkplain Citation#getTitle() titles} without identifiers.</p>
     *
     * @param  c1 The first citation to compare, or {@code null}.
     * @param  c2 the second citation to compare, or {@code null}.
     * @return {@code true} if both arguments are non-null, and at least one identifier,
     *         title or alternate title matches.
     */
    public static boolean identifierMatches(Citation c1, Citation c2) {
        if (c1 != null && c2 != null) {
            if (c1 == c2) {
                return true; // Optimisation for a common case.
            }
            /*
             * If there is no identifier in both citations, fallback on title comparisons.
             * If there is identifiers in only one citation, make sure that this citation
             * is the second one (c2) in order to allow at least one call to
             * 'identifierMatches(c1, String)'.
             */
            Iterator<? extends Identifier> iterator = iterator(c2.getIdentifiers());
            if (iterator == null) {
                iterator = iterator(c1.getIdentifiers());
                if (iterator == null) {
                    return titleMatches(c1, c2);
                }
                c1 = c2;
            }
            do {
                final Identifier id = iterator.next();
                if (id != null && identifierMatches(c1, id.getCode())) {
                    return true;
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    /**
     * Returns {@code true} if any {@linkplain Citation#getIdentifiers() identifiers} in the given
     * citation matches the given string. The comparison is case-insensitive and ignores leading
     * and trailing spaces.
     *
     * <p>If (and <em>only</em> if) the citation does not contain any identifier, then this method
     * fallback on titles comparison using the {@link #titleMatches(Citation,String) titleMatches}
     * method. This fallback exists for compatibility with client codes using citation
     * {@linkplain Citation#getTitle() titles} without identifiers.</p>
     *
     * @param  citation The citation to check for, or {@code null}.
     * @param  identifier The identifier to compare, or {@code null}.
     * @return {@code true} if both arguments are non-null, and the title or alternate title
     *         matches the given string.
     */
    public static boolean identifierMatches(final Citation citation, String identifier) {
        if (citation != null && identifier != null) {
            identifier = identifier.trim();
            final Iterator<? extends Identifier> identifiers = iterator(citation.getIdentifiers());
            if (identifiers == null) {
                return titleMatches(citation, identifier);
            }
            while (identifiers.hasNext()) {
                final Identifier id = identifiers.next();
                if (id != null) {
                    final String code = id.getCode();
                    if (code != null && identifier.equalsIgnoreCase(code.trim())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the shortest identifier for the specified citation, or the title if there is
     * no identifier. This method is useful for extracting the namespace from an authority,
     * for example {@code "EPSG"}.
     *
     * @param  citation The citation for which to get the identifier, or {@code null}.
     * @return The shortest identifier of the given citation, or {@code null} if the
     *         given citation was null or doesn't declare any identifier or title.
     */
    public static String getIdentifier(final Citation citation) {
        String identifier = null;
        if (citation != null) {
            final Iterator<? extends Identifier> it = iterator(citation.getIdentifiers());
            if (it != null) while (it.hasNext()) {
                final Identifier id = it.next();
                if (id != null) {
                    String candidate = id.getCode();
                    if (candidate != null) {
                        candidate = candidate.trim();
                        final int length = candidate.length();
                        if (length != 0) {
                            if (identifier == null || length < identifier.length()) {
                                identifier = candidate;
                            }
                        }
                    }
                }
            }
            if (identifier == null) {
                final InternationalString title = citation.getTitle();
                if (title != null) {
                    identifier = title.toString();
                }
            }
        }
        return identifier;
    }
}