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

import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.ServerInterface;
import info.debatty.java.aggregation.OWA;

/**
 *
 * @author georgi
 */
public class OrderedWeightedAverage implements DetectionAgentInterface {
    private static final double[] DEFAULT_OWA_WEIGHTS = {0.2, 0.4, 0.3, 0.1};

    @Override
    public void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        double[] owa_weights = DEFAULT_OWA_WEIGHTS;
        try {
            String owa_w_1 = profile.getParameter("owa_weight_1");
            String owa_w_2 = profile.getParameter("owa_weight_2");
            String owa_w_3 = profile.getParameter("owa_weight_3");
            String owa_w_4 = profile.getParameter("owa_weight_4");
            if (owa_w_1 != null && owa_w_2 != null
                    && owa_w_3 != null && owa_w_4 != null) {
                owa_weights[0] = Double.valueOf(owa_w_1);
                owa_weights[1] = Double.valueOf(owa_w_2);
                owa_weights[2] = Double.valueOf(owa_w_3);
                owa_weights[3] = Double.valueOf(owa_w_4);
            }
        } catch (Exception ex) {
            System.out.println("Could not get the OWA weight parameters"
                    + " from configuration file. Error: " + ex.getMessage());
        }

        Evidence[] evidences = datastore.findLastEvidences(
                profile.getTriggerLabel(), event.getSubject());
        OWA owa_aggregator = new OWA(owa_weights);

        //create the evidence
        Evidence ev = new Evidence();

        //ev.setScore(owa.aggregate());
        ev.setSubject(event.getSubject());
        ev.setTime(event.getTimestamp());
        ev.setReport("OWA Aggregation generated for evidences with"
                + " label " + profile.getTriggerLabel());
        datastore.addEvidence(ev);

    }
    
}
