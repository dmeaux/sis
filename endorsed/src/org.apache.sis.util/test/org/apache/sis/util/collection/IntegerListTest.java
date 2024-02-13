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
package org.apache.sis.util.collection;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.ConcurrentModificationException;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import static java.lang.StrictMath.*;

// Test dependencies
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.sis.test.TestCase;
import org.apache.sis.test.TestUtilities;
import static org.apache.sis.test.Assertions.assertSerializedEquals;
import static org.apache.sis.test.Assertions.assertParallelStreamEquals;
import static org.apache.sis.test.Assertions.assertSequentialStreamEquals;


/**
 * Tests {@link IntegerList} implementations.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 */
public final class IntegerListTest extends TestCase {
    /**
     * The list of integers being tested.
     */
    private IntegerList list;

    /**
     * Creates a new test case.
     */
    public IntegerListTest() {
    }

    /**
     * Writes values and read them again for making sure they are the expected ones.
     * This method tests also split iterators.
     *
     * @param maximalValue  the maximal value allowed.
     */
    private void testReadWrite(final int maximalValue) {
        final Random random = TestUtilities.createRandomNumberGenerator();
        final int length = 350 + random.nextInt(101);
        /*
         * Use half the lenght as initial capacity in order to test dynamic resizing.
         * Dynamic resizing should happen in one of the call to list.add(value) after
         * about 200 values.
         */
        list = new IntegerList(length / 2, maximalValue);
        assertTrue(list.maximalValue() >= maximalValue);
        final List<Integer> copy = new ArrayList<>(length);
        for (int i=0; i<length; i++) {
            assertEquals(i, list.size());
            final Integer value = nextInt(random, maximalValue);
            assertTrue(copy.add(value));
            assertTrue(list.add(value));
        }
        assertEquals(copy, list, "Comparison with reference implementation");
        assertEquals(copy.hashCode(), list.hashCode());
        /*
         * Overwrite about 1/10 of the values in both the tested IntegerList and the
         * reference ArrayList. Then compare the IntegerList against the reference.
         */
        for (int i=0; i<length; i += 8 + random.nextInt(5)) {
            final Integer value = nextInt(random, maximalValue);
            final Integer old = copy.set(i, value);
            assertNotNull(old);
            assertEquals (old, list.set(i, value));
        }
        for (int i=0; i<length; i++) {
            if (!copy.get(i).equals(list.get(i))) {
                fail("Mismatched value at index " + i);
            }
        }
        assertEquals(copy, list, "Comparison with reference implementation");
        assertEquals(copy.hashCode(), list.hashCode());
        /*
         * Test the stream, using the ArrayList as a reference implementation. This will indirectly
         * use the PrimitiveSpliterator.forEachRemaining(Consumer<? super Integer>) method. A more
         * specific test using forEachRemaining(IntConsumer) is done by the testInts() method.
         */
        assertSequentialStreamEquals(copy.iterator(), list.stream());
        /*
         * Tests cloning and removal of values in a range of indices. The IntegerList.removeRange(…)
         * method is invoked indirectly by subList(…).clear(). Again, we use ArrayList as a reference.
         */
        final IntegerList clone = list.clone();
        assertEquals(copy, clone);
        assertEquals(copy.remove(100), clone.remove(100));
        assertEquals(copy, clone);
        copy .subList(128, 256).clear();
        clone.subList(128, 256).clear();
        assertEquals(copy, clone);
        /*
         * Tests iterator on primitive integers, with random removal of some elements during traversal.
         */
        final PrimitiveIterator.OfInt it = clone.iterator();
        final Iterator<Integer> itRef = copy.iterator();
        while (itRef.hasNext()) {
            assertTrue(it.hasNext());
            assertEquals(itRef.next().intValue(), it.nextInt());
            if (random.nextInt(10) == 0) {
                itRef.remove();
                it.remove();
            }
        }
        assertFalse(it.hasNext());
        assertEquals(copy, clone, "After remove()");
        /*
         * Verify that serialization and deserialization gives a new list with identical content.
         */
        assertNotSame(list, assertSerializedEquals(list));
    }

    /**
     * Returns the next number from the random number generator.
     */
    private static int nextInt(final Random random, final int maximalValue) {
        if (maximalValue == Integer.MAX_VALUE) {
            return abs(random.nextInt());
        } else {
            return random.nextInt(maximalValue + 1);
        }
    }

    /**
     * Tests the fill value using the existing list, which is assumed
     * already filled with random values prior this method call.
     */
    private void testFill(final int value) {
        final Set<Integer> set = new HashSet<>();
        list.fill(value);
        set.addAll(list);
        assertEquals(Set.of(value), set, "fill(value)");
        list.fill(0);
        set.clear();
        set.addAll(list);
        assertEquals(Set.of(0), set, "fill(0)");
    }

    /**
     * Tests with a maximal value of 1.
     */
    @Test
    public void test1() {
        testReadWrite(1);
        testFill(1);
    }

    /**
     * Tests with a maximal value of 2.
     */
    @Test
    public void test2() {
        testReadWrite(2);
        testFill(2);
    }

    /**
     * Tests with a maximal value of 3.
     */
    @Test
    public void test3() {
        testReadWrite(3);
        testFill(3);
    }

    /**
     * Tests with a maximal value of 10.
     */
    @Test
    public void test10() {
        testReadWrite(10);
        testFill(10);
    }

    /**
     * Tests with a maximal value of 100.
     */
    @Test
    public void test100() {
        testReadWrite(100);
        final int old100 = list.getInt(100);
        list.resize(101);
        assertEquals(old100, list.getInt(100), "getInt(last)");
        list.resize(200);
        assertEquals(   200, list.size());
        assertEquals(old100, list.getInt(100), "getInt(existing)");
        assertEquals(     0, list.getInt(101), "getInt(new)");
        for (int i=101; i<200; i++) {
            assertEquals(0, list.getInt(i));
        }
        list.resize(400);
        testFill(100);
    }

    /**
     * Tests with a maximal value of 100000.
     */
    @Test
    public void test100000() {
        testReadWrite(100000);
        testFill(17);
    }

    /**
     * Tests with a maximal value of {@value Integer#MAX_VALUE}.
     */
    @Test
    public void testMax() {
        testReadWrite(Integer.MAX_VALUE);
        testFill(17);
    }

    /**
     * Tests that primitive stream traversal is coherent with its list value.
     * This method tests sequential stream only.
     */
    @Test
    public void testStream() {
        list = createRandomlyFilled(42, 404);
        list.stream(false).forEach(new IntConsumer() {
            private int index = 0;

            @Override
            public void accept(int value) {
                assertEquals(list.getInt(index++), value, "Spliterator value differs from its original list");
            }
        });
    }

    /**
     * Tests that primitive stream traversal with parallelization.
     */
    @Test
    public void testIntsParallel() {
        list = createRandomlyFilled(80, 321);
        assertParallelStreamEquals(list.iterator(), list.stream().parallel());
    }

    /**
     * Ensures our stream is a fail-fast operator, i.e: it fails when the list has
     * been modified before the end of its iteration.
     */
    @Test
    public void testErrorOnCoModification() {
        list = createRandomlyFilled(4, 10);
        final PrimitiveIterator.OfInt values = list.stream(false).iterator();

        // Start iteration normally.
        assertEquals(list.getInt(0), values.nextInt());
        assertEquals(list.getInt(1), values.nextInt());

        // Now, if we alter the list and then try to use previously created stream, we should get an error.
        list.add(0);
        var e = assertThrows(ConcurrentModificationException.class, () -> values.next(),
                             "Concurrent modification has not been detected.");
        assertNotNull(e);
    }

    /**
     * Creates a new list whose capacity and value magnitude are defined as input.
     * The list is filled by a random integer generator before return.
     *
     * @param  size      number of elements to insert in the list.
     * @param  maxValue  maximum value to use for value insertion.
     * @return a fresh and filled list.
     */
    private static IntegerList createRandomlyFilled(final int size, final int maxValue) {
        final Random random = TestUtilities.createRandomNumberGenerator();
        return IntStream.generate(() -> random.nextInt(maxValue))
                .limit(size)
                .collect(() -> new IntegerList(size, maxValue), IntegerList::addInt, (l1, l2) -> l1.addAll(l2));
    }
}
