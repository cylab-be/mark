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
import java.util.Map;

/**
 * This operator produces a score only if the score of the triggering algorithm
 * is above a threshold value. Takes only one parameter:
 * <ul>
 * <li><code>value</code> (default 0.5)</li>
 * </ul>
 *
 * Example configuration (threshold10.detection.yml):
 *
 * <pre>
 * ---
 * class_name:     be.cylab.mark.detection.Threshold
 * label:          detection.threshold.10
 * trigger_label:  detection.2h.count
 * parameters: {
 *   value : 10
 * }
 * </pre>
 * @author tibo
 */
public class Threshold implements DetectionAgentInterface {

    private static final double DEFAULT_VALUE = 0.5;

    @Override
    public final void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        Map<String, String> subject = event.getSubject();

        double value = profile.getParameterDouble("value", DEFAULT_VALUE);

        Evidence old_evidence =
                datastore.findEvidenceById(event.getId());

        if (old_evidence.getScore() < value) {
            return;
        }

        Evidence new_ev = new Evidence();
        new_ev.references().add(old_evidence.getId());

        String report =
                "Report " + old_evidence.getId() + " had a score of "
                + old_evidence.getScore();

        new_ev.setSubject(subject);
        new_ev.setTime(event.getTimestamp());
        new_ev.setScore(old_evidence.getScore());
        new_ev.setReport(report);
        datastore.addEvidence(new_ev);
    }

}
