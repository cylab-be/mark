/*
 * The MIT License
 *
 * Copyright 2017 georgi.
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
package netrank;

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author Georgi Nikolov
 * Agent tests the ratio of POST methods used by a client-server connection.
 * if the ratio exceeds a specified threshold it is tagged as suspicious.
 */
public class POSTCount implements DetectionAgentInterface<Link> {

    private static final double DEFAULT_POST_THRESHOLD = 0.4;
    private static final String THRESHOLD_STRING = "threshold";

    /**
     * Analyze function inherited from the DetectionAgentInterface.
     * accepts the subject to analyze
     * trigger of the agent
     * the profile used to load the agent
     * the database to which to connect to gather RawData
     * @param subject
     * @param actual_trigger_label
     * @param profile
     * @param datastore
     * @throws java.lang.Throwable
     */
    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        //check for parameters set through the config file
        double threshold = DEFAULT_POST_THRESHOLD;
        String threshold_string = profile.parameters.get(THRESHOLD_STRING);
        if (threshold_string != null) {
            try {
                threshold = Double.valueOf(threshold_string);
            } catch (NumberFormatException ex) {
                threshold = DEFAULT_POST_THRESHOLD;
            }
        }

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        if (raw_data.length < 50) {
            return;
        }

        int number_of_post = 0;
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            if (data.contains("POST")) {
                number_of_post = number_of_post + 1;
            }
        }

        double post_percentage = (double) number_of_post
                / raw_data.length;

        if (post_percentage > threshold) {
            Evidence evidence = new Evidence();
            evidence.score = post_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a connection between: "
                    + "<br /> client " + subject.getClient()
                    + " and server " + subject.getServer()
                    + "<br /> where the ratio of connections with method POST "
                    + "compared to all other connection methods has a ratio of:"
                    + " " + post_percentage + "\n"
                    + "<br /> Number of entries analysed: " + raw_data.length
                    + "<br /> The POSTcount ratio threshold for suspicious "
                    + "activity is " + threshold;

            datastore.addEvidence(evidence);
        }
    }

}
