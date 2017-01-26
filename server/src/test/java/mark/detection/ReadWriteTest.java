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

package mark.detection;

import java.net.URL;
import junit.framework.TestCase;
import mark.client.Client;
import mark.server.DummySuject;
import mark.server.DummySubjectAdapter;
import mark.core.Evidence;
import mark.core.RawData;
import mark.server.Config;
import mark.server.Server;

/**
 *
 * @author Thibault Debatty
 */
public class ReadWriteTest extends TestCase {

    private Server server;

    @Override
    protected void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    /**
     * Test of run method, of class ReadWrite.
     */
    public void testRun() throws Throwable  {
        System.out.println("run Read Write detection agent");

        // Start a dummy server
        server = new Server(new Config());
        server.start();

        // create a connexion to the server
        Client<DummySuject> client = new Client<DummySuject>(
                new URL("http://127.0.0.1:8080"), new DummySubjectAdapter());

        // What we will work with...
        DummySuject subject = new DummySuject();
        subject.name = "A person of interest";

        // Search existing data with tag "manual.data"
        RawData<DummySuject>[] original = client.findRawData(
                "manual.data", subject);

        // Add a rawdata
        RawData<DummySuject> data = new RawData<DummySuject>();
        data.data = "blah...";
        data.label = "manual.data";
        data.subject = subject;
        data.time = 123456789;
        client.addRawData(data);

        // Search data with same tag, there should be one additional entry
        RawData<DummySuject>[] after_insert = client.findRawData(
                "manual.data", subject);
        assertEquals(original.length + 1, after_insert.length);


        Evidence<DummySuject>[] evidence_original = client.findEvidence(
                "manual.detection", subject);

        // Manually run a readwrite detection task
        // Should insert two evidences...
        ReadWrite<DummySuject> instance = new ReadWrite<DummySuject>();
        instance.setInputLabel("manual.data");
        instance.setLabel("manual.detection");
        instance.setSubject(subject);
        instance.setDatastoreUrl(new URL("http://127.0.0.1:8080"));
        instance.setSubjectAdapter(new DummySubjectAdapter());
        instance.run();

        Evidence<DummySuject>[] evidence_after_insert = client.findEvidence(
                "manual.detection", subject);

        assertEquals(
                evidence_original.length + 2, evidence_after_insert.length);

    }

}
