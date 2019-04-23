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

    /**
     * Test for the setMembership function of FuzzyLogic 
     */
    public final void testSetMembership() {
        System.out.println("Test FuzzyLogic setMembership \n");
        double test_value1 = 0.6;
        double test_value2 = 2;
        double test_value3 = 0.1;
        System.out.println("Run test one with normal values");
        double[][] test1 = {{0.4, 0}, {0.9, 1}};
        try {
        double result = fuzzylogic.setMembership(test1, test_value1);
        System.out.println("Result: " + result);
        //assertEquals(result1, 0.4);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with value > upper bound X");
        try {
        double result = fuzzylogic.setMembership(test1, test_value2);
        System.out.println("Result: " + result);
        assertEquals(result, 1.0);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with value < lower bound X");
        try {
        double result = fuzzylogic.setMembership(test1, test_value3);
        System.out.println("Result: " + result);
        assertEquals(result, 0.0);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test where x1 = x2");
        double[][] test2 = {{0.4, 0}, {0.4, 1}};
        try {
        double result = fuzzylogic.setMembership(test2, test_value1);
        System.out.println("Result: " + result);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test where y1 = y2");
        double[][] test3 = {{0.4, 1}, {0.9, 1}};
        try {
        double result = fuzzylogic.setMembership(test3, test_value1);
        System.out.println("Result: " + result);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public final void testFuzzyAnd() {
        System.out.println("Test FuzzyLogic fuzzyAnd \n");
        double[] test_values1 = {0.4, 1, 0.2};
        double[] test_values2 = {0.4, 0.4, 0.4};
        double[] test_values3 = {-0.4, 0.3, 0.4};
        double[] test_values4 = {};
        System.out.println("run test with normal values");
        try {
        double result1 = fuzzylogic.fuzzyAnd(test_values1);
        assertEquals(result1, 0.2);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with all the same values given");
        try {
        double result2 = fuzzylogic.fuzzyAnd(test_values2);
        assertEquals(result2, 0.4);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with bad value lower than 0");
        try {
        double result3 = fuzzylogic.fuzzyAnd(test_values3);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with empty values array given");
        try {
        double result4 = fuzzylogic.fuzzyAnd(test_values4);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

        public final void testFuzzyOr() {
        System.out.println("Test FuzzyLogic fuzzyOr \n");
        double[] test_values1 = {0.4, 0.9, 0.2};
        double[] test_values2 = {0.4, 0.4, 0.4};
        double[] test_values3 = {2, 0.3, 0.4};
        double[] test_values4 = {};
        System.out.println("run test with normal values");
        try {
        double result1 = fuzzylogic.fuzzyOr(test_values1);
        assertEquals(result1,0.9);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with all the same values given");
        try {
        double result2 = fuzzylogic.fuzzyOr(test_values2);
        assertEquals(result2, 0.4);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with bad value lower than 0");
        try {
        double result3 = fuzzylogic.fuzzyOr(test_values3);
        } catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("run test with empty values array given");
        try {
        double result4 = fuzzylogic.fuzzyOr(test_values4);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

}
