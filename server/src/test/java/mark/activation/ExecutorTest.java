/*
 * The MIT License
 *
 * Copyright 2019 Thibault Debatty.
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

import junit.framework.TestCase;
import mark.core.DetectionAgentProfile;
import mark.core.InvalidProfileException;
import mark.core.RawData;
import mark.server.Config;
import mark.server.DummySubject;

/**
 *
 * @author georgi
 */
public class ExecutorTest extends TestCase {

    public void testActivation()
            throws InvalidProfileException, InterruptedException {
        Config config = Config.getTestConfig();
        ExecutorInterface executor = new DummyExecutor();
        ActivationController controller =
                new ActivationController(config, executor);

        RawData<DummySubject> data = new RawData();
        data.label = "data.dummy";
        data.subject = new DummySubject("dummy subject");
        data.time = 123456;
        data.data = "A proxy log line...";

        DetectionAgentProfile profile = new DetectionAgentProfile();
        profile.trigger_label = "data.dummy";
        profile.class_name = mark.detection.DummyDetector.class.getCanonicalName();
        controller.addAgent(profile);
        controller.start();

        Thread.sleep(1000);
        controller.notifyRawData(data);
        Thread.sleep(2000);

        assertEquals(1, executor.taskCount());

    }

}
