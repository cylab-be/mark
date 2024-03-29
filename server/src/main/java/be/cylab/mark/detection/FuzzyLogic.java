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
package be.cylab.mark.detection;

/**
 * Implements fuzzy logic membership function.
 *
 * Compute f(x) = a * x + b from two points (x1, y1) and (x2, y2).
 *
 * @author Georgi Nikolov
 */
public final class FuzzyLogic {

    private final double x1;
    private final double x2;
    private final double y1;
    private final double y2;

    /**
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    public FuzzyLogic(
            final double x1, final double x2, final double y1,
            final double y2) {

        //test if x1 is equal to x2 because it may cause division by 0
        if (x1 == x2) {
            throw new IllegalArgumentException("X1 == X2 -> can't divide by 0");
        }

        if (x1 > x2) {
            throw new IllegalArgumentException("x2 must be > x1");
        }

        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    /**
     * Compute the membership functions given two points.
     *
     * @param value
     * @return
     */
    public double determineMembership(final double value) {

        //receives ex. (x1,y1) = [90,0] and (x2,y2) = [100,1]
        //determine if value is < x1 or > x2
        //because f(x) = m*x + b is a membership function it has soft limits
        //which are f(x1) and f(x2), anything below or
        //above that is considered equal to y1 or y2 respectively
        //where the result will be between (0,1)
        if (value < x1) {
            return y1;
        } else if (value > x2) {
            return y2;
        }
        //the function we need to determine if f(x)=m*x + b
        //first determine the slope m
        double slope_m = (y2 - y1) / (x2 - x1);
        //once we have the slope m we can substitute m,x and y in the function
        //and determine the coef b
        double coeff_b = y1 - (x1 * slope_m);
        //we have the slope m and the coefficient b, now we can substitute them
        //in the function and determine f(value)
        return slope_m * value + coeff_b;
    }

    /**
     * Method for determining the min() of an array of values.
     * @param values
     * @return
     */
    public double fuzzyAnd(final double[] values) {
        if (values.length == 0) {
            //if the values supplied are empty throw exception
            throw new IllegalArgumentException("Values array given is empty");
        }
        double result = 1;
        //loop through the values to find the smallest one
        for (double value : values) {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("Bad values provided");
            }
            if (value <= result) {
                result = value;
            }
        }
        //return the smallest value as result
        return result;
    }

    /**
     *
     * @param values
     * @return
     */
    public double fuzzyOr(final double[] values) {
         if (values.length == 0) {
            //if the values supplied are empty throw exception
            throw new IllegalArgumentException("Values array given is empty");
        }
        double result = 0;
        //loop through the values to find the largest one
        for (double value : values) {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("Bad values provided");
            }
            if (value >= result) {
                result = value;
            }
        }
        //return the largest value as result
        return result;
    }
}
