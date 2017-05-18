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
package mark.integration;

import com.maxmind.geoip.LookupService;
import java.io.IOException;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.core.DetectionAgentProfile;
import netrank.FileSource;
import netrank.LinkAdapter;
import mark.server.Config;
import mark.server.Server;
import mark.core.DataAgentProfile;
import netrank.GeoOutlier;

/**
 *
 * @author georgi
 */
public class HTTPGeoOutlierIT extends TestCase {

    private Server server;

    @Override
    protected final void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        super.tearDown();
    }
    
    public final void testLoadGeoIP() throws IOException {
        System.out.println("test we can load the GeoIP DB from JAR");
        System.out.println("======================================");
        GeoOutlier detector = new GeoOutlier();
        LookupService cl = detector.loadGeoIP();
        assertNotNull(cl);
    }

    public final void testFrequencyAgent()
            throws Throwable {

        System.out.println("test geo-outlier agent");
        System.out.println("======================");

        Config config = Config.getTestConfig();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Configure a single data source (HTTP, Regex, file with 2k reqs)
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                "file",
                getClass().getResource("/1000_http_requests.txt")
                        .getPath());

        DataAgentProfile http_source = new DataAgentProfile();
        http_source.class_name = FileSource.class.getCanonicalName();
        http_source.label = "data.http";
        http_source.parameters = parameters;

        server.addDataAgentProfile(http_source);

        // Activate the dummy detection agent
        server.addDetectionAgent(
                DetectionAgentProfile.fromInputStream(
                        getClass()
                                .getResourceAsStream("/detection.http.geooutlier.yml")));
        server.start();
        server.awaitTermination();
    }

}
