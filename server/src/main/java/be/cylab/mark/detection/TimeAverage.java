/*
 * The MIT License
 *
 * Copyright 2019 tibo.
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
import java.time.Instant;
import java.util.Map;

/**
 *
 * @author tibo
 */
public class TimeAverage implements DetectionAgentInterface {

    private static final String DEFAULT_WINDOW = String.valueOf(7 * 24 * 3600);

    @Override
    public final void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        String label = event.getLabel();
        Map<String, String> subject = event.getSubject();

        long window = Long.valueOf(
                profile.getParameterOrDefault("window", DEFAULT_WINDOW));

        long start_time = event.getTimestamp() - window * 1000;

        Evidence[] evidences =
                datastore.findEvidenceSince(label, subject, start_time);

        Evidence new_ev = new Evidence();
        double sum = 0;
        for (Evidence ev : evidences) {
            sum += ev.getScore();
            new_ev.references().add(ev.getId());
        }
        double score = sum / evidences.length;

        String report =
                "Found <b>" + evidences.length + "</b> evidences with label "
                + "<b>" + event.getLabel() + "</b> since "
                + Instant.ofEpochMilli(start_time).toString() + "<br>"
                + "Average = " + sum + " / " + evidences.length + " = "
                + score;

        new_ev.setSubject(subject);
        new_ev.setTime(event.getTimestamp());
        new_ev.setScore(score);
        new_ev.setReport(report);
        datastore.addEvidence(new_ev);
    }

}
