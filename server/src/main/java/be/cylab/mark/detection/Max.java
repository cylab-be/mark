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

import be.cylab.mark.core.ClientWrapperInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * This operator keeps the highest score of evidences reports produced during
 * a time window. It takes only one parameter:
 * <ul>
 * <li><code>time_window</code> in seconds (default 3600 - 1h)</li>
 * </ul>
 *
 * Example configuration (2h.max.detection.yml):
 *
 * <pre>
 * ---
 * class_name:     be.cylab.mark.detection.Max
 * label:          detection.2h.max
 * trigger_label:  detection.2h.count
 * parameters: {
 *   time_window : 7200
 * }
 * </pre>
 *
 * @author Thibault Debatty
 */
public class Max extends AbstractDetection {

    private static final int DEFAULT_TIME_WINDOW = 3600;

    @Override
    public final void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ClientWrapperInterface datastore) throws Throwable {

        String label = event.getLabel();
        Map<String, String> subject = event.getSubject();

        long time_window = profile.getParameterInt(
                "time_window", DEFAULT_TIME_WINDOW);

        long till = event.getTimestamp();
        long from = till - (time_window * 1000);

        Evidence[] evidences =
                datastore.findEvidenceSince(label, subject, from);

        Evidence new_ev = new Evidence();
        double max = 0;
        for (Evidence ev : evidences) {
            if (ev.getScore() > max) {
                max = ev.getScore();
            }
            new_ev.references().add(ev.getId());
        }

        // String report =testmail@example.be
        //         "Found <b>" + evidences.length + "</b> evidences with label "
        //         + "<b>" + event.getLabel() + "</b> since "
        //         + Instant.ofEpochMilli(from).toString() + "<br>"
        //         + "Highest score was " + max;
        HashMap<String, Object> param = new HashMap<String, Object>();
        // param.put("subject", subject);
        param.put("score", max);
        param.put("length", evidences.length);
        param.put("label", event.getLabel());
        param.put("evidences", evidences);
        param.put("since", from);
        new_ev.setSubject(subject);
        new_ev.setTime(event.getTimestamp());
        new_ev.setScore(max);
        new_ev.setReport(this.make_report(new Object[]{param}));
        datastore.addEvidence(new_ev);
    }

}
