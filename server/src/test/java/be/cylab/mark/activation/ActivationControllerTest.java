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

import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import java.util.Map;
import junit.framework.TestCase;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.RawData;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.DummySubject;
import java.io.InputStream;
import java.util.LinkedList;

/**
 *
 * @author Georgi Nikolov
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

    public void testAddingLastTimeTriggeredEntry() throws Throwable, InvalidProfileException {
        System.out.println("Test Adding new Entry to LastTimeTriggered");

        ActivationController<DummySubject> controller = getTestController();

        //create a test RawData to be added to event Map
        RawData data = getTestData();
        //create a dummy agent and add it to the controller
        DetectionAgentProfile profile = getTestDetectionAgent();
        LinkedList<DetectionAgentProfile> profiles = new LinkedList<>();
        profiles.add(profile);
        //key that we will be looking for
        String key = profile.getClassName() + "-" + data.getSubject().toString();
        controller.setProfiles(profiles);
        controller.start();

        Thread.sleep(1000);
        //notify controller of new RawData
        controller.notifyRawData(data);
        Thread.sleep(1000);
        //add a new RawData with different Subject
        data.setSubject(new DummySubject("dummi subject2"));
        data.setTime(new Long(123477));
        //new key to be used for testing
        String key2 = profile.getClassName() + "-" + data.getSubject().toString();

        controller.notifyRawData(data);
        Thread.sleep(2000);
        //check that there is a new entry added to LastTimeTriggered
        Map<String, Long> last_time_triggered = controller.getLastTimeTriggered();

        assertEquals(2, last_time_triggered.size());
        assertEquals(true, last_time_triggered.keySet().contains(key));
        assertEquals(true, last_time_triggered.keySet().contains(key2));
        assertEquals(Long.valueOf(123456), last_time_triggered.get(key));
        assertEquals(Long.valueOf(123477), last_time_triggered.get(key2));
    }

    public void testUpdatingLastTimeTriggeredEntry() throws Throwable, InvalidProfileException {
        System.out.println("Test Updating Entry of LastTimeTriggered");

        ActivationController<DummySubject> controller = getTestController();

        //create a test RawData to be added to event Map
        RawData data = getTestData();
        //create a dummy agent and add it to the controller
        DetectionAgentProfile profile = getTestDetectionAgent();
        //key that we will be looking for
        String key = profile.getClassName() + "-" + data.getSubject().toString();
        LinkedList<DetectionAgentProfile> profiles = new LinkedList<>();
        profiles.add(profile);
        controller.setProfiles(profiles);
        controller.start();

        Thread.sleep(1000);
        //notify controller of new RawData
        controller.notifyRawData(data);
        Thread.sleep(1000);
        //add a new RawData with different timestamp > trigger interval of the agent
        data.setTime(new Long(234567));

        controller.notifyRawData(data);
        Thread.sleep(1000);
        //add a new RawData with different timestamp < trigger interval of the agent
        data.setTime(new Long(234600));

        controller.notifyRawData(data);
        Thread.sleep(2000);

        Map<String, Long> last_time_triggered = controller.getLastTimeTriggered();
        //check that there is a new correct entry added to LastTimeTriggered
        assertEquals(1, last_time_triggered.size());
        assertEquals(true, last_time_triggered.keySet().contains(key));
        //check that the entry was updated with the second timestamp
        assertEquals(Long.valueOf(234567), last_time_triggered.get(key));
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
        evidence.setSubject(new DummySubject("dummi subject"));
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

    private DetectionAgentProfile getTestDetectionAgent() {
        InputStream stream = getClass()
                .getResourceAsStream("/detection.dummy.yml");

        DetectionAgentProfile profile =
                DetectionAgentProfile.fromInputStream(stream);
        return profile;
    }
}
