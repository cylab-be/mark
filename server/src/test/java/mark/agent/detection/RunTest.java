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

package mark.agent.detection;

import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class RunTest extends TestCase {


    /**
     * Test of run method, of class Run.
     */
    public final void testRun() {
        System.out.println("run");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(Run.KEY_COMMAND, "echo");
        parameters.put(Run.KEY_WD, "/tmp");
        parameters.put("param1", "value1");

        Run run_external_detector = new Run();
        run_external_detector.setDatastoreUrl("http://dummy.to.u:1234");
        run_external_detector.setParameters(parameters);
        run_external_detector.run();
    }

}
