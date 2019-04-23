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

/** Class responsible for implementing fuzzy logic membership function.
 *
 * @author georgi
 */
public class FuzzyLogic {
    //fuzzy logic function is f(x)=m*x + b
    //we need to calculate the mean and coefficient b to be able to calculate
    //the f(x) for given point x

    /**
     * Method for calculating the membership functions given two points.
     * Lower bound (x1,y1) and upper bound (x2,y2) will be used to calculate the
     * membership function and then calculate f(value) of this function. The 
     * function returns a value between (0,1).
     * @param fuzzy_parameters
     * @param value
     * @return
     */

    public double setMembership(double[][] fuzzy_parameters,
            double value) {
        double result;
        //extract lower_bound and upper_bound and get the x and y for each bound
        double[] lower_bound = fuzzy_parameters[0];
        double[] upper_bound = fuzzy_parameters[1];
        double lower_bound_x = lower_bound[0];
        double lower_bound_y = lower_bound[1];
        double upper_bound_x = upper_bound[0];
        double upper_bound_y = upper_bound[1];
        //check if x1 = x2, if that is the case 
        //throw error as we can't divide by 0
        if (lower_bound_x == upper_bound_x) {
            throw new ArithmeticException(
                    "Lower bound X equal to upper bound X. / by zero");
        }
        //check if y1 = y2, if that is the case
        //throw error as we want soft threshold between (0,1)
        if (lower_bound_y == upper_bound_y) {
            throw new ArithmeticException(
                    "Lower bound Y equal to upper bound Y."
                            + " Expected function bound between (0,1)");
        }
        //the function we need to determine if f(x)=m*x + b
        //first determine the mean m
        double mean_m = (upper_bound_y - lower_bound_y) /
                (upper_bound_x - lower_bound_x);
        //once we have the mean m we can substitute m,x and y in the function
        //and determine the coef b
        double lower_coeff_b = lower_bound_y - (lower_bound_x * mean_m);
        double upper_coeff_b = upper_bound_y - (upper_bound_x * mean_m);
        if (lower_coeff_b != upper_coeff_b) {
            throw new ArithmeticException(
                    "Error determining membership function, lower coefficient b"
                            + "is not equal to upper coefficient b");
        }
        //we have the mean m and the coefficient b, now we can substitute them
        //in the function and determine f(value)
        double f_value = ( mean_m * value ) + lower_coeff_b;
        //determine if f_value is < lower bound or > upper_bound
        //because f(x) = m*x + b is a membership function it has soft limits
        //which are f(lower_bound_x) and f(upper_bound_x), anything below or
        //above that is considered equal to lower or upper bound respectively
        //where the result will be between (0,1)
        if (f_value < lower_bound_y) {
            result = lower_bound_y;
        } else if (f_value > upper_bound_y) {
            result = upper_bound_y;
        } else {
            result = f_value;
        }
        return result;
    }

    /**
     * Method for determining the min() of an array of values.
     * @param values
     * @return
     */
    public double fuzzyAnd(double[] values) {
        if (values.length == 0) {
            //if the values supplied are empty throw exception
            throw new NullPointerException("Values array given is empty");
        }
        double result = 1;
        //loop through the values to find the smallest one
        for (double value : values) {
            if (value <= result) {
                result = value;
            }
        }
        //check if the result is between 0 and 1
        if (result < 0 || result > 1) {
            throw new ArithmeticException("The values given to fuzzyAnd must"
                    + "be between 0 and 1, was given: " + result);
        }
        //return the smallest value as result
        return result;
    }

    public double fuzzyOr(double[] values) {
         if (values.length == 0) {
            //if the values supplied are empty throw exception
            throw new NullPointerException("Values array given is empty");
        }
        double result = 0;
        //loop through the values to find the largest one
        for (double value : values) {
            if (value >= result) {
                result = value;
            }
        }
        //check if the result is between 0 and 1
        if (result < 0 || result > 1) {
            throw new ArithmeticException("The values given to fuzzyOr must"
                    + "be between 0 and 1, was given: " + result);
        }
        //return the largest value as result
        return result;
    }
}
