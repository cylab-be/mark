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

import junit.framework.TestCase;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.server.DummySubject;

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
        System.out.println("test Frequency agent with 60 sec interval,"
                + "1000 apt connections and 10000 noise connections");
        FrequencyTestClient<DummySubject> client =
                new FrequencyTestClient<>(1000, 10000, 60);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequency2() throws Throwable {
        System.out.println("test Frequency agent with 60 sec interval,"
                + "30 APT connections and 100 noise connections");

        FrequencyTestClient client =
                new FrequencyTestClient(30, 100, 60);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequency3() throws Throwable {
        System.out.println("test Frequency agent with 60 sec interval,"
                + "30 APT connections and 1000 noise connections");

        FrequencyTestClient client =
                new FrequencyTestClient(30, 1000, 60);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequencyWithVeryHighInterval() throws Throwable {
        System.out.println("test Frequency agent with 1800 sec interval,"
                + "40 APT connections and 100 noise connections");

        FrequencyTestClient client =
                new FrequencyTestClient(40, 100, 1800);

        testWithClient(client);
        assertEquals(1, client.getEvidences().size());
    }

    public void testFrequencyWithNoApt() throws Throwable {
        System.out.println("test Frequency agent with no APT connections");

        FrequencyTestClient client =
                new FrequencyTestClient(0, 1000, 0);

        testWithClient(client);
        assertEquals(0, client.getEvidences().size());
    }

    private void testWithClient(FrequencyTestClient<DummySubject> client)
            throws Throwable {
                Frequency agent = new Frequency();

        DetectionAgentProfile profile = DetectionAgentProfile.fromInputStream(
                getClass().getResourceAsStream("/detection.frequency.yaml"));

        Event event = new Event("actual.trigger",
                new DummySubject("foo"),
                123456,
                "1");

        agent.analyze(
                event,
                profile,
                client);
    }

}
