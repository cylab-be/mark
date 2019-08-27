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

import java.util.HashMap;
import java.util.Map;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.ServerInterface;
import mark.core.Subject;

/**
 *
 * @author Thibault Debatty
 */
public class Average implements DetectionAgentInterface {

    private static final int DEFAULT_MIN_DENOMINATOR = 3;
    private static final String DENOMINATOR_STRING = "mindenominator";

    @Override
    public void analyze(
            final Subject subject,
            final long timestamp,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        //check for parameters set through the config file
        int min_denominator = DEFAULT_MIN_DENOMINATOR;
        String threshold_string = String.valueOf(
                profile.getParameter(DENOMINATOR_STRING));
        if (threshold_string != null) {
            try {
                min_denominator = Integer.valueOf(threshold_string);
            } catch (NumberFormatException ex) {
                min_denominator = DEFAULT_MIN_DENOMINATOR;
            }
        }

        Evidence[] evidences = datastore.findLastEvidences(
                profile.getTriggerLabel(), subject);

        //check if the amount of evidences gotten from the DB is equal or higher
        //to the minimum needed to aggregate. If not the case, use the default
        //min_denominator, otherwise use the number of evidences available
        int denominator = min_denominator;
        if (evidences.length >= min_denominator) {
            denominator = evidences.length;
        }

        //map to hold the agents triggered and the score they produce
        Map<String, String[]> agent_labels = new HashMap<>();

        double score = 0;
        long last_time = 0;
        for (Evidence ev : evidences) {
            score += ev.getScore() / denominator;

            if (ev.getTime() > last_time) {
                last_time = ev.getTime();
            }

            //add the agent and his score to the map
            agent_labels.put(ev.getLabel(),
                    new String[]{Double.toString(ev.getScore()), ev.getId()});
        }

        //create a string of the agent_labels to be added to the report
        String agents_output = "";
        for (String key : agent_labels.keySet()) {
            agents_output = agents_output + "<br />Agent("
                    + key + ") : Score(" + agent_labels.get(key)[0]
                    + ") : Id(" + agent_labels.get(key)[1] + ")";
        }

        Evidence ev = new Evidence();
        ev.setLabel(profile.getLabel());
        ev.setScore(score);
        ev.setSubject(subject);
        ev.setTime(last_time);
        ev.setReport("Average Aggregation generated for evidences with"
                + " label " + profile.getTriggerLabel()
                + "<br /> Agents used for the aggregation: "
                + agents_output);
        datastore.addEvidence(ev);
    }

}
