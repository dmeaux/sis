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
package org.apache.sis.internal.test;

import org.apache.sis.test.TestCase;
import org.apache.sis.test.XMLComparator;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link XMLComparator} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.17)
 * @version 0.3
 * @module
 */
public final strictfp class XMLComparatorTest extends TestCase {
    /**
     * Tests the {@link XMLComparator#ignoredAttributes} and {@link XMLComparator#ignoredNodes} sets.
     *
     * @throws Exception Should never happen.
     */
    @Test
    public void testIgnore() throws Exception {
        final XMLComparator cmp = new XMLComparator(
            "<body>\n" +
            "  <form id=\"MyForm\">\n" +
            "    <table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </table>\n" +
            "  </form>\n" +
            "</body>",
            "<body>\n" +
            "  <form id=\"MyForm\">\n" +
            "    <table cellpading=\"2\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </table>\n" +
            "  </form>\n" +
            "</body>");

        ensureFail("Should fail because the \"cellpading\" attribute value is different.", cmp);

        // Following comparison should not fail anymore.
        cmp.ignoredAttributes.add("cellpading");
        cmp.compare();

        cmp.ignoredAttributes.clear();
        cmp.ignoredAttributes.add("bgcolor");
        ensureFail("The \"cellpading\" attribute should not be ignored anymore.", cmp);

        // Ignore the table node, which contains the faulty attribute.
        cmp.ignoredNodes.add("table");
        cmp.compare();

        // Ignore the form node and all its children.
        cmp.ignoredNodes.clear();
        cmp.ignoredNodes.add("form");
        cmp.compare();
    }

    /**
     * Ensures that the call to {@link XMLComparator#compare()} fails. This method is
     * invoked in order to test that the comparator rightly detected an error that we
     * were expected to detect.
     *
     * @param message The message for JUnit if the comparison does not fail.
     * @param cmp The comparator on which to invoke {@link XMLComparator#compare()}.
     */
    private static void ensureFail(final String message, final XMLComparator cmp) {
        try {
            cmp.compare();
        } catch (AssertionError e) {
            return;
        }
        fail(message);
    }
}