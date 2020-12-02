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

import junit.framework.TestCase;
import info.debatty.java.aggregation.OWA;

/**
 *
 * @author Georgi Nikolov
 */
public class OWATest extends TestCase {
    private static final double[] WEIGHTS = new double[]{0.2, 0.4, 0.3, 0.1};
    private static final OWA OWA_AGGREGATOR = new OWA(WEIGHTS);

    public final void testOWAwithEqualNumberOfScoresToWeights() throws Throwable {
        double[] scores = new double[]{1, 0.8, 0.6, 0.1};
        double expected_value = 0.71;
        double result = OWA_AGGREGATOR.aggregate(scores);
        assertEquals(expected_value, result, 0.0);
    }
}
