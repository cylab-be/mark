/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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

import be.cylab.mark.DummySubject;
import junit.framework.TestCase;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import static junit.framework.TestCase.assertTrue;

/**
 * Test the Frequency detector.
 *
 * To run only this test:
 * cd server
 * mvn test -Dtest=be.cylab.mark.detection.FrequencyTest
 *
 * @author Thibault Debatty
 */
public class FrequencyTest extends TestCase {

    /**
     * Test of run method, of class Frequency.
     * @throws java.lang.Throwable
     */
    public void testFrequency1() throws Throwable {
        System.out.println("Frequency agent with 300 sec interval, "
                + "0 noise connections");
        FrequencyTestClient client = new FrequencyTestClient(0, 300);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequency2() throws Throwable {
        System.out.println("Frequency agent with 300 sec interval, "
                + "1000 noise connections");
        FrequencyTestClient client =
                new FrequencyTestClient(1000, 300);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequency3() throws Throwable {
        System.out.println("Frequency agent with 300 sec interval, "
                + "2000 noise connections");
        FrequencyTestClient client =
                new FrequencyTestClient(2000, 300);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequency4() throws Throwable {
        System.out.println("Frequency agent with 1800 sec interval, "
                + "48 noise connections");
        FrequencyTestClient client =
                new FrequencyTestClient(100, 1800);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }


    public void testFrequencyWithNoApt() throws Throwable {
        System.out.println("Frequency agent with no APT, "
                + "1000 noise connections");
        FrequencyTestClient client =
                new FrequencyTestClient(1000, 86400);

        testWithClient(client);

        LinkedList<Evidence> evidences = client.getEvidences();
        if (evidences.isEmpty()) {
            return;
        }

        assertTrue(evidences.get(0).getScore() < 0.2);
    }

    public void testFrequencyFigures() throws Throwable {
        System.out.println("Frequency agent producing 1 evidence, test if"
                + " figures are correctly generated and saved");

        FrequencyTestClient client = new FrequencyTestClient(0, 300);

        testWithClient(client);
        List<Evidence> evidences = client.getEvidences();
        assertEquals(3, evidences.get(0).figures().size());
    }

    private void testWithClient(FrequencyTestClient client)
            throws Throwable {
                Frequency agent = new Frequency();

        DetectionAgentProfile profile = DetectionAgentProfile.fromInputStream(
                getClass().getResourceAsStream("/detection.frequency.yaml"));

        Date date = new Date();
        long now = date.getTime() / 1000; // in seconds

        Event event = new Event("actual.trigger",
                new DummySubject("foo"),
                now,
                "");

        agent.analyze(event, profile, client);
    }

}
