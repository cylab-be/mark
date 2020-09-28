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

/**
 *
 * @author tibo
 */
public final class Counter implements DetectionAgentInterface {

    @Override
    public void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        long till = event.getTimestamp();
        long from = till - 24 * 3600;

        int count = datastore.findRawData(
                event.getLabel(), event.getSubject(), from, till).length;

        Evidence ev = new Evidence();
        ev.setReport(
                "Found " + count + " data entries for "
                + event.getSubject().toString());

        ev.setScore(count);
        ev.setSubject(event.getSubject());
        ev.setTime(event.getTimestamp());
        datastore.addEvidence(ev);

    }

}
