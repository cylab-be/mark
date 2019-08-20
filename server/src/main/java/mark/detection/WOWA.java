/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.ServerInterface;
import mark.core.Subject;

/**
 *
 * @author Thibault Debatty
 */
public class WOWA implements DetectionAgentInterface {

    @Override
    public void analyze(
            final Subject subject,
            final long timestamp,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        Evidence[] evidences = datastore.findEvidence(
                actual_trigger_label, subject);

        // Each detector has the same weight
        double[] weights = new double[evidences.length];
        for (int i = 0; i < evidences.length; i++) {
            weights[i] = 1.0 / evidences.length;
        }

        //
        double[] ordered_weights = new double[evidences.length];
        ordered_weights[0] = 0.5;
        ordered_weights[1] = 0.5;

        // info.debatty.java.aggregation.WOWA wowa =
        //        new info.debatty.java.aggregation.WOWA(
        //                weights, ordered_weights);

    }

}
