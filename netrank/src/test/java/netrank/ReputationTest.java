/*
 * The MIT License
 *
 * Copyright 2017 georgi.
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
package netrank;

import java.util.LinkedList;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;

/**
 *
 * @author georgi
 */
public class ReputationTest extends TestCase{

    public void testAnalyze() throws Throwable {
        System.out.println("analyze Reputation test" + "\n");

        Reputation agent = new Reputation();
        ExtendedDummyClient client = new ExtendedDummyClient();
        agent.analyze(
                new Link("192.168.2.3", "github.com"),
                "actual.trigger",
                DetectionAgentProfile.fromInputStream(
                        getClass().getResourceAsStream(
                                "/detection.reputation.yaml")),
                client);

        LinkedList<Evidence> evidences = client.getEvidences();
        //assertEquals(1, evidences.size());

    }
    
    
}
