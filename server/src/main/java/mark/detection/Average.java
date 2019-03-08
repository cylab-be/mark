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
public class Average implements DetectionAgentInterface {

    @Override
    public void analyze(
            final Subject subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        Evidence[] evidences = datastore.findLastEvidences(
                profile.trigger_label, subject);

        if (evidences.length < 3) {
            return;
        }

        double score = 0;
        long last_time = 0;
        for (Evidence ev : evidences) {
            score += ev.score / evidences.length;

            if (ev.time > last_time) {
                last_time = ev.time;
            }
        }

        Evidence ev = new Evidence();
        ev.label = profile.label;
        ev.report = "Average Aggregation generated for evidences with"
                    + " label " + profile.trigger_label;
        ev.score = score;
        ev.subject = subject;
        ev.time = last_time;
        datastore.addEvidence(ev);



    }

}
