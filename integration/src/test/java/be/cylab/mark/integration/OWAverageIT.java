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
package be.cylab.mark.integration;

import be.cylab.mark.client.Client;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import java.net.URL;
import static junit.framework.TestCase.assertEquals;

/**
 *
 * @author georgi
 */
public class OWAverageIT extends MarkCase {

    public final void testOrderedWeightedAverage() throws Throwable {
        server = getServer();

        DetectionAgentProfile agent = new DetectionAgentProfile();
        agent.setClassName("be.cylab.mark.detection.OWAverage");
        agent.setLabel("detection.owa");
        agent.setTriggerLabel("data.");
        server.addDetectionAgent(agent);

        server.start();

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());

        Link link = new Link("1.2.3.4", "my.server.com");

        Evidence ev = new Evidence();
        ev.setLabel("data.test1");
        ev.setScore(1);
        ev.setSubject(link);
        ev.setTime(12341);
        datastore.addEvidence(ev);

        Evidence ev2 = new Evidence();
        ev2.setLabel("data.test2");
        ev2.setScore(0.8);
        ev2.setSubject(link);
        ev2.setTime(12342);
        datastore.addEvidence(ev2);

        Thread.sleep(3000);
        Evidence[] owa_evidences =
                datastore.findLastEvidences("detection.owa", link);

        assertEquals(1, owa_evidences.length);
        assertEquals(0.52,
                owa_evidences[0].getScore(),
                0.0);

        Evidence ev3 = new Evidence();
        ev3.setLabel("data.test3");
        ev3.setScore(0.6);
        ev3.setSubject(link);
        ev3.setTime(12343);
        datastore.addEvidence(ev3);

        Evidence ev4 = new Evidence();
        ev4.setLabel("data.test4");
        ev4.setScore(0.2);
        ev4.setSubject(link);
        ev4.setTime(12344);
        datastore.addEvidence(ev4);

        Evidence ev5 = new Evidence();
        ev5.setLabel("data.test5");
        ev5.setScore(0.2);
        ev5.setSubject(link);
        ev5.setTime(12345);
        datastore.addEvidence(ev5);

        Thread.sleep(3000);
        Evidence[] new_owa_evidences =
                datastore.findLastEvidences("detection.owa", link);

        assertEquals(1, new_owa_evidences.length);
        assertEquals(0.72,
                new_owa_evidences[0].getScore(),
                0.0);
    }
}
