/*
 * The MIT License
 *
 * Copyright 2018 georgi.
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
package mark.integration;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.core.DataAgentProfile;
import mark.server.Config;
import mark.server.Server;
import netrank.ArchiveSource;
import netrank.LinkAdapter;

/**
 *
 * @author georgi
 */
public class HTTPArchiveSourceIT extends TestCase {
    private Server server;

    @Override
    protected final void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    /**
     *
     * @throws Exception
     */
    public final void testHTTPArchiveSource() throws Throwable {

        System.out.println("Test with a HTTP archive source");
        System.out.println("============================");

        Config config = Config.getTestConfig();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Configure a single data source (HTTP, Regex, file with 1000 reqs)
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/example_json_proxy.gz").getPath());

        DataAgentProfile example_json_proxy = new DataAgentProfile();
        example_json_proxy.class_name = ArchiveSource.class.getCanonicalName();
        example_json_proxy.parameters = parameters;
        example_json_proxy.label = "data.http";
        server.addDataAgentProfile(example_json_proxy);

        server.start();

        // Wait for data sources and detection to finish...
        server.awaitTermination();
    }
}
