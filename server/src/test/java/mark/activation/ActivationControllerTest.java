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
package mark.activation;

import java.util.Map;
import junit.framework.TestCase;
import mark.core.Evidence;
import mark.core.InvalidProfileException;
import mark.core.RawData;
import mark.core.Subject;
import mark.server.Config;
import mark.server.DummySubject;

/**
 *
 * @author georgi
 * @param <T>
 */
public class ActivationControllerTest<T extends Subject> extends TestCase {

    public void testNotifyRawDataWithDifferentSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyRawData With Different Subjects");
        System.out.println("==========================");
        Config config = Config.getTestConfig();
        ActivationController activation_controller =
                new ActivationController(config);
        //create a RawData entry to be pushed to the Activation Controller
        RawData data = new RawData();
        data.label = "data.http";
        data.subject = new DummySubject("dummy subject");
        data.time = (long) (System.currentTimeMillis() / 1000L);
        data.data = "A proxy log line...";

        //notify the Activation Controller that new data is available
        activation_controller.notifyRawData(data);

        //add a second RawData entry with different subject
        data.subject = new DummySubject("another dummy subject 2");
        data.time = (long) (System.currentTimeMillis() / 1000L);

        //notify the Activation Controller that new data is available
        activation_controller.notifyRawData(data);

        //get the events map from the Activation Controller
        Map<String, Map<T, Long>> event_map = activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.label));

        //check for two instances of different subjects under the label
        Map<T, Long> subject_map = event_map.get(data.label);
        assertEquals(2, subject_map.keySet().size());
    }

    public void testNotifyRawDataWithSameSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyRawData With Same Subjects");
        System.out.println("==========================");
        Config config = Config.getTestConfig();
        ActivationController activation_controller =
                new ActivationController(config);
        //create a RawData entry to be pushed to the Activation Controller
        RawData data = new RawData();
        data.label = "data.http";
        data.subject = new DummySubject("dummy subject");
        data.time = 123456;
        data.data = "A proxy log line...";

        //notify the Activation Controller that new data is available
        activation_controller.notifyRawData(data);

        //add a second RawData entry with the same subject but later timestamp
        data.time = 456789;

        //notify the Activation Controller that new data is available
        activation_controller.notifyRawData(data);

        //get the events map from the Activation Controller
        Map<String, Map<T, Long>> event_map = activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.label));

        //check for one instance for the subject under the label
        Map<T, Long> subject_map = event_map.get(data.label);
        assertEquals(1, subject_map.keySet().size());
        //check that it uses only the latest timestamp
        long map_timestamp = subject_map.get(data.subject);
        assertEquals(456789, map_timestamp);
    }

        public void testNotifyEvidenceWithDifferentSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyEvidence With Different Subjects");
        System.out.println("==========================");
        Config config = Config.getTestConfig();
        ActivationController activation_controller =
                new ActivationController(config);
        //create a RawData entry to be pushed to the Activation Controller
        Evidence data = new Evidence();
        data.label = "evidence.http";
        data.subject = new DummySubject("dummy subject");
        data.time = (long) (System.currentTimeMillis() / 1000L);
        data.report = "A proxy log line...";

        //notify the Activation Controller that new data is available
        activation_controller.notifyEvidence(data);

        //add a second RawData entry with different subject
        data.subject = new DummySubject("another dummy subject 2");
        data.time = (long) (System.currentTimeMillis() / 1000L);

        //notify the Activation Controller that new data is available
        activation_controller.notifyEvidence(data);

        //get the events map from the Activation Controller
        Map<String, Map<T, Long>> event_map = activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.label));

        //check for two instances of different subjects under the label
        Map<T, Long> subject_map = event_map.get(data.label);
        assertEquals(2, subject_map.keySet().size());
    }

    public void testNotifyEvidenceWithSameSubjects()
            throws InvalidProfileException, Throwable {
        System.out.println("Test NotifyEvidence With Same Subjects");
        System.out.println("==========================");
        Config config = Config.getTestConfig();
        ActivationController activation_controller =
                new ActivationController(config);
        //create a RawData entry to be pushed to the Activation Controller
        Evidence data = new Evidence();
        data.label = "evidence.http";
        data.subject = new DummySubject("dummy subject");
        data.time = 123456;
        data.report = "A proxy log line...";

        //notify the Activation Controller that new data is available
        activation_controller.notifyEvidence(data);

        //add a second RawData entry with the same subject but later timestamp
        data.time = 456789;

        //notify the Activation Controller that new data is available
        activation_controller.notifyEvidence(data);

        //get the events map from the Activation Controller
        Map<String, Map<T, Long>> event_map = activation_controller.getEvents();
        //check that the data was saved under the correct label
        assertEquals(true, event_map.keySet().contains(data.label));

        //check for one instance for the subject under the label
        Map<T, Long> subject_map = event_map.get(data.label);
        assertEquals(1, subject_map.keySet().size());
        //check that it uses only the latest timestamp
        long map_timestamp = subject_map.get(data.subject);
        assertEquals(456789, map_timestamp);
    }
}
