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
package be.cylab.mark.activation;

import junit.framework.TestCase;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.RawData;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.DummySubject;

/**
 *
 * @author Georgi Nikolov
 */
public class ExecutorTest extends TestCase {

    public void testActivation()
            throws InvalidProfileException, InterruptedException {
        Config config = Config.getTestConfig();
        ExecutorInterface executor = new DummyExecutor();
        ActivationController controller =
                new ActivationController(config, executor);

        RawData<DummySubject> data = new RawData();
        data.setLabel("data.dummy");
        data.setSubject(new DummySubject("dummy subject"));
        data.setTime(123456);
        data.setData("A proxy log line...");

        DetectionAgentProfile profile = new DetectionAgentProfile();
        profile.setTriggerLabel("data.dummy");
        profile.setClassName(
                be.cylab.mark.detection.DummyDetector.class.getCanonicalName());
        controller.setAgentProfile(profile);
        controller.start();

        Thread.sleep(1000);
        controller.notifyRawData(data);
        Thread.sleep(2000);

        assertEquals(1, executor.getStatus().get("executed"));

    }

}
