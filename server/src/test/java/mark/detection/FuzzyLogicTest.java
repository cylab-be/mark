/*
 * The MIT License
 *
 * Copyright 2019 georgi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mark.detection;

import junit.framework.TestCase;

/**
 *
 * @author georgi
 */
public class FuzzyLogicTest extends TestCase {
    FuzzyLogic fuzzylogic = new FuzzyLogic();
    double test_value1 = 0.6;
    double test_value2 = 2;
    double test_value3 = 0.1;
    double x1 = 0.4;
    double x2 = 0.9;
    double y1 = 0;
    double y2 = 1;

    /**
     * Test for the setMembership function of FuzzyLogic 
     */
    public final void testSetMembershipWithValidValues() {
        System.out.println("Run FuzzyLogic test one with normal values");
        double result = fuzzylogic.setMembership(x1, x2, y1, y2, test_value1);
        assertEquals(0.3999999999999999, result);
    }

    public final void testSetMembershipWithValueBiggerThanX2() {
        System.out.println("run test with value > x2");
        double result = fuzzylogic.setMembership(x1, x2, y1, y2, test_value2);
        assertEquals(1.0, result);
    }
    
    public final void testSetMembershipWithValueSmallerThanX1() {
        System.out.println("run test with value < x1");
        double result = fuzzylogic.setMembership(x1, x2, y1, y2, test_value3);
        assertEquals(0.0, result);
    }
    
    public final void testSetMembershipWhereX1EqualToX2() {
        System.out.println("run test where x1 = x2");
        try {
            double result = fuzzylogic.setMembership(
                    x1, x1, y1, y2, test_value1);
            fail();
        } catch (ArithmeticException ex) {
            assertEquals("X1 == X2 -> can't divide by 0", ex.getMessage());
        }
    }
    
    public final void testSetMembershipWhereY1EqualToY2() {
        System.out.println("run test where y1 = y2");
        double result = fuzzylogic.setMembership(x1, x2, y2, y2, test_value1);
        assertEquals(1.0, result);
    }


    double[] fuzzyand_values1 = {0.4, 1, 0.2};
    double[] fuzzyand_values2 = {0.4, 0.4, 0.4};
    double[] fuzzyand_values3 = {-0.4, 0.3, 0.4};
    double[] fuzzyand_values4 = {};

    /**
     * Test the FuzzyAnd method
     */
    public final void testFuzzyAndWithNormalValues() {
        System.out.println("run test with normal values");
        double result = fuzzylogic.fuzzyAnd(fuzzyand_values1);
        assertEquals(0.2, result);
    }
    
    public final void testFuzzyAndWithAllTheSameValues() {
        System.out.println("run test with all the same values given");
        double result = fuzzylogic.fuzzyAnd(fuzzyand_values2);
        assertEquals(0.4, result);
    }

    public final void testFuzzyAndWithBadValuesLowerThan0() {
        System.out.println("run test with bad value lower than 0");  
        try {
            double result = fuzzylogic.fuzzyAnd(fuzzyand_values3);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals("Bad values provided", ex.getMessage());
        }
    }

    public final void testFuzzyAndWithEmptyArray() {
        System.out.println("run test with empty values array given");
        try {
            double result = fuzzylogic.fuzzyAnd(fuzzyand_values4);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals("Values array given is empty", ex.getMessage());
        }
    }

    double[] fuzzyor_values1 = {0.4, 0.9, 0.2};
    double[] fuzzyor_values2 = {0.4, 0.4, 0.4};
    double[] fuzzyor_values3 = {2, 0.3, 0.4};
    double[] fuzzyor_values4 = {};

    /**
     * Test the FuzzyOr method
     */
    public final void testFuzzyOrWithNormalValues() {
    System.out.println("run test with normal values");

    double result = fuzzylogic.fuzzyOr(fuzzyor_values1);
    assertEquals(0.9, result);
    }
    
    public final void testFuzzyOrWithAllTheSameValues() {
    System.out.println("run test with all the same values given");
    double result = fuzzylogic.fuzzyOr(fuzzyor_values2);
    assertEquals(0.4, result);
    }
    
    public final void testFuzzyOrWithBadValuesLowerThan0() {
    System.out.println("run test with bad value lower than 0");
        try {
            double result = fuzzylogic.fuzzyOr(fuzzyand_values3);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals("Bad values provided", ex.getMessage());
        }
    }
    
    public final void testFuzzyOrWithEmptyArray() {
    System.out.println("run test with empty values array given");
        try {
            double result = fuzzylogic.fuzzyOr(fuzzyand_values4);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals("Values array given is empty", ex.getMessage());
        }
    }
}
