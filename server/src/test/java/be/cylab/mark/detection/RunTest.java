/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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

/**
 *
 * @author Thibault Debatty
 */
public class RunTest extends TestCase {


    /**
     * Test of analyze method, of class Run.
     */
    public final void testAnalyze() throws Throwable {
        System.out.println("run");

        Run run_detector = new Run();

        DetectionAgentProfile agent =
                DetectionAgentProfile.fromInputStream(getClass()
                .getResourceAsStream("/detection.run.yml"));

        run_detector.analyze(
                new Event(
                        "actual.trigger",
                        new DummySubject("Tibo"),
                        123456,
                        "1234"),
                agent,
                new DummyClient());
    }

}
