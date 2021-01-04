/*
 * The MIT License
 *
 * Copyright 2020 tibo.
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
package be.cylab.mark.data;

import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.RawData;
import be.cylab.mark.detection.DummyClient;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.code.regexp.Pattern;
import com.google.code.regexp.Matcher;

/**
 * Test the FileSource data injector. To run only this test:
 * mvn -Dtest=be.cylab.mark.data.FileSourceTest test
 * @author tibo
 */
public class FileSourceTest {

    @Test
    public void testNamedRegex() {
        String pattern = "(?<foo>\\w+) world";
        String line = "hello world!";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(line);
        matcher.find();
        assertEquals("hello", matcher.group("foo"));

        pattern = "^(?<timestamp>\\d+\\.\\d+)\\s+\\d+\\s(?<client>\\d+\\.\\d+\\.\\d+\\.\\d+)";
        line = "1472083259.891    850 253.115.106.54 TCP_MISS/200 561 GET http://tnlewhi.euxfwdmro.ad/mjfmsynwf.html - DIRECT/54.132.254.155 text/html";
        regex = Pattern.compile(pattern);
        matcher = regex.matcher(line);
        assertTrue(matcher.find());
        assertEquals("1472083259.891", matcher.group("timestamp"));
    }

    /**
     * Test of run method, of class FileSource.
     * @throws java.lang.Throwable
     */
    @Test
    public void testRun() throws Throwable {
        System.out.println("named regex file source");

        DataAgentProfile profile = new DataAgentProfile();
        profile.setLabel("data");
        profile.setParameter(
                "file",
                getClass().getResource("/1000_http_requests.txt").getPath());
        profile.setParameter(
                "regex",
                "^(?<timestamp>\\d+)\\.\\d+\\s+\\d+\\s(?<client>\\d+\\.\\d+\\.\\d+\\.\\d+)");

        DummyClient datastore = new DummyClient();
        FileSource instance = new FileSource();
        instance.run(profile, datastore);

        RawData data_0 = datastore.getData().get(0);
        assertEquals("198.36.158.8", data_0.getSubject().get("client"));
    }

}
