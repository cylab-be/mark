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
package be.cylab.mark.activation;

import be.cylab.mark.core.Event;
import java.util.Map;
import junit.framework.TestCase;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.RawData;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.DummySubject;

/**
 *
 * @author georgi
 */
public class ActivationControllerTest extends TestCase {

    public void testNotifyRawDataWithDifferentSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyRawData With Different Subjects");

        ActivationController activation_controller = getTestController();

        //create a RawData entry to be pushed to the Activation Controller
        RawData data = this.getTestData();
        activation_controller.notifyRawData(data);

        //add a second RawData entry with different subject
        data.setSubject(new DummySubject("another dummy subject 2"));
        data.setTime((long) (System.currentTimeMillis() / 1000L));

        //notify the Activation Controller that new data is available
        activation_controller.notifyRawData(data);

        //get the events map from the Activation Controller
        Map<String, Map<DummySubject, Long>> event_map
                = activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.getLabel()));

        //check for two instances of different subjects under the label
        Map<DummySubject, Long> subject_map = event_map.get(data.getLabel());
        assertEquals(2, subject_map.keySet().size());
    }

    public void testNotifyRawDataWithSameSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyRawData With Same Subjects");

        ActivationController activation_controller = getTestController();

        // create a RawData entry to be pushed to the Activation Controller
        RawData<DummySubject> data = this.getTestData();
        activation_controller.notifyRawData(data);

        //add a second RawData entry with the same subject but later timestamp
        data.setTime(456789);
        activation_controller.notifyRawData(data);

        //get the events map from the Activation Controller
        Map<String, Map<DummySubject, Event<DummySubject>>> event_map =
                activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.getLabel()));

        //check for one instance for the subject under the label
        Map<DummySubject, Event<DummySubject>> subject_map =
                event_map.get(data.getLabel());
        assertEquals(1, subject_map.keySet().size());

        //check that it uses only the latest timestamp
        long map_timestamp = subject_map.get(data.getSubject()).getTimestamp();
        assertEquals(456789, map_timestamp);
    }

    public void testNotifyEvidenceWithDifferentSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyEvidence With Different Subjects");

        ActivationController activation_controller = getTestController();

        //create an Evidence entry to be pushed to the Activation Controller
        Evidence evidence = getTestEvidence();
        activation_controller.notifyEvidence(evidence);

        //add a second Evidence entry with different subject
        evidence.setSubject(new DummySubject("another dummy subject 2"));
        evidence.setTime((long) (System.currentTimeMillis() / 1000L));
        activation_controller.notifyEvidence(evidence);

        //get the events map from the Activation Controller
        Map<String, Map<DummySubject, Long>> event_map = activation_controller.getEvents();

        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(evidence.getLabel()));

        //check for two instances of different subjects under the label
        Map<DummySubject, Long> subject_map = event_map.get(evidence.getLabel());
        assertEquals(2, subject_map.keySet().size());
    }

    public void testNotifyEvidenceWithSameSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyEvidence With Same Subjects");

        ActivationController<DummySubject> controller = getTestController();

        //create a Evidence entry to be pushed to the Activation Controller
        Evidence<DummySubject> evidence = getTestEvidence();
        controller.notifyEvidence(evidence);

        //add a second Evidence entry with the same subject but later timestamp
        evidence.setTime(456789);
        controller.notifyEvidence(evidence);

        //get the events map from the Activation Controller
        Map<String, Map<DummySubject, Event<DummySubject>>> events =
                controller.getEvents();

        //check that the data was saved under the correct label
        assertEquals(true, events.keySet().contains(evidence.getLabel()));

        //check for one instance for the subject under the label
        Map<DummySubject, Event<DummySubject>> subjects =
                events.get(evidence.getLabel());
        assertEquals(1, subjects.keySet().size());

        //check that it uses only the latest timestamp
        Event<DummySubject> event = subjects.get(evidence.getSubject());
        assertEquals(456789, event.getTimestamp());
    }

    public void testLabelsMatching() throws InvalidProfileException {
        ActivationController<DummySubject> controller
                = this.getTestController();

        assertTrue(controller.checkLabelsMatch("data.http", "data.http"));
        assertTrue(controller.checkLabelsMatch("data.http", "data.http.123"));

        assertTrue(controller.checkLabelsMatch("data.http..*", "data.http.123"));
        assertTrue(controller.checkLabelsMatch("^data.http", "data.http.123"));
        assertTrue(controller.checkLabelsMatch("data.*.123", "data.http.123"));
    }

    /**
     * Create a single instance of RawData that can inserted for testing...
     *
     * @return
     */
    private RawData<DummySubject> getTestData() {
        RawData<DummySubject> data = new RawData();
        data.setLabel("data.http");
        data.setSubject(new DummySubject("dummy subject"));
        data.setTime(123456);
        data.setData("A proxy log line...");
        return data;
    }

    private Evidence<DummySubject> getTestEvidence() {
        Evidence evidence = new Evidence();
        evidence.setLabel("evidence.http");
        evidence.setSubject(new DummySubject("dummy subject"));
        evidence.setTime(123456);
        evidence.setReport("Some report...");
        return evidence;
    }

    private ActivationController<DummySubject> getTestController()
            throws InvalidProfileException {
        Config config = Config.getTestConfig();
        ExecutorInterface executor = new DummyExecutor();
        return new ActivationController(config, executor);
    }
}
