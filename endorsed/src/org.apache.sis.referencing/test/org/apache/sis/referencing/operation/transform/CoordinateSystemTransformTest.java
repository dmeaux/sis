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
package org.apache.sis.referencing.operation.transform;

import org.opengis.util.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.DefaultGeocentricCRS;
import org.apache.sis.referencing.cs.CoordinateSystems;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.measure.Units;
import org.apache.sis.util.ArraysExt;

// Test dependencies
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengis.test.referencing.TransformTestCase;
import org.apache.sis.test.FailureDetailsReporter;
import org.apache.sis.referencing.cs.HardCodedCS;


/**
 * Tests the {@link CoordinateSystemTransform} static factory method.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
@ExtendWith(FailureDetailsReporter.class)
public final class CoordinateSystemTransformTest extends TransformTestCase {
    /**
     * A right-handed spherical coordinate system.
     */
    private final SphericalCS spherical;

    /**
     * The builder to use for creating the transforms.
     */
    private final CoordinateSystemTransformBuilder builder;

    /**
     * Creates the {@link MathTransformFactory} to be used for the tests.
     * We do not use the system-wide factory in order to have better tests isolation.
     */
    public CoordinateSystemTransformTest() {
        spherical = (SphericalCS) DefaultGeocentricCRS.castOrCopy(CommonCRS.WGS84.spherical())
                            .forConvention(AxesConvention.RIGHT_HANDED).getCoordinateSystem();
        builder = new CoordinateSystemTransformBuilder(DefaultMathTransformFactory.provider());
    }

    /**
     * Returns the given coordinate system but with linear axes in centimetres instead of metres.
     */
    private static CoordinateSystem toCentimetres(final CoordinateSystem cs) {
        return CoordinateSystems.replaceLinearUnit(cs, Units.CENTIMETRE);
    }

    /**
     * Returns {@link SphericalToCartesianTest#testData()} modified for the source and target
     * coordinate systems used in this class.
     */
    private static double[][] sphericalTestData() {
        final double[][] data = SphericalToCartesianTest.testData();
        final double[] source = data[0];
        for (int i=0; i<source.length; i += 3) {
            ArraysExt.swap(source, i, i+1);
        }
        final double[] target = data[1];
        for (int i=0; i<target.length; i++) {
            target[i] *= 100;
        }
        return data;
    }

    /**
     * Creates the transform and stores the result in {@link #transform}.
     */
    private void createTransform(final CoordinateSystem source, final CoordinateSystem target) throws FactoryException {
        builder.setSourceAxes(source, null);
        builder.setTargetAxes(target, null);
        transform = builder.create();
    }

    /**
     * Verifies that {@code builder.getMethod()} has the expected value.
     */
    private void assertMethodEquals(final String expected) {
        final OperationMethod method = builder.getMethod().orElseThrow();
        assertEquals(expected, method.getName().getCode());
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion between two spherical coordinate systems.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testSphericalToSpherical() throws FactoryException, TransformException {
        createTransform(HardCodedCS.SPHERICAL, spherical);
        final double[][] data = SphericalToCartesianTest.testData();
        final double[] source = data[0];
        final double[] target = data[1];
        System.arraycopy(source, 0, target, 0, source.length);
        for (int i=0; i<source.length; i += 3) {
            ArraysExt.swap(source, i, i+1);
        }
        verifyTransform(source, target);
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from spherical to Cartesian coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testSphericalToCartesian() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(HardCodedCS.SPHERICAL, toCentimetres(HardCodedCS.GEOCENTRIC));
        final double[][] data = sphericalTestData();
        verifyTransform(data[0], data[1]);
        assertMethodEquals("Spherical to Cartesian");
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from Cartesian to spherical coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testCartesianToSpherical() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(toCentimetres(HardCodedCS.GEOCENTRIC), HardCodedCS.SPHERICAL);
        final double[][] data = sphericalTestData();
        verifyTransform(data[1], data[0]);
        assertMethodEquals("Cartesian to spherical");
    }

    /**
     * Returns {@link PolarToCartesianTest#testData(boolean)} modified for the source and target
     * coordinate systems used in this class.
     */
    private static double[][] polarTestData(final boolean withHeight) {
        final int dimension = withHeight ? 3 : 2;
        final double[][] data = PolarToCartesianTest.testData(withHeight);
        final double[] source = data[0];
        for (int i=1; i<source.length; i += dimension) {
            source[i] = -source[i];                 // Change counterclockwise direction into clockwise direction.
        }
        final double[] target = data[1];
        for (int i=0; i<target.length; i++) {
            target[i] *= 100;
        }
        return data;
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from cylindrical to Cartesian coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testCylindricalToCartesian() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(HardCodedCS.CYLINDRICAL, toCentimetres(HardCodedCS.CARTESIAN_3D));
        final double[][] data = polarTestData(true);
        verifyTransform(data[0], data[1]);
        assertMethodEquals("Cylindrical to Cartesian");
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from Cartesian to cylindrical coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testCartesianToCylindrical() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(toCentimetres(HardCodedCS.CARTESIAN_3D), HardCodedCS.CYLINDRICAL);
        final double[][] data = polarTestData(true);
        verifyTransform(data[1], data[0]);
        assertMethodEquals("Cartesian to cylindrical");
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from polar to Cartesian coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testPolarToCartesian() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(HardCodedCS.POLAR, toCentimetres(HardCodedCS.CARTESIAN_2D));
        final double[][] data = polarTestData(false);
        verifyTransform(data[0], data[1]);
        assertMethodEquals("Polar to Cartesian");
    }

    /**
     * Tests {@link CoordinateSystemTransform#create CoordinateSystemTransform.create(…)}
     * for a conversion from Cartesian to polar coordinates.
     *
     * @throws FactoryException if an error occurred while creating the transform.
     * @throws TransformException if an error occurred while transforming the test point.
     */
    @Test
    public void testCartesianToPolar() throws FactoryException, TransformException {
        tolerance = 1E-9;
        createTransform(toCentimetres(HardCodedCS.CARTESIAN_2D), HardCodedCS.POLAR);
        final double[][] data = polarTestData(false);
        verifyTransform(data[1], data[0]);
        assertMethodEquals("Cartesian to polar");
    }
}
