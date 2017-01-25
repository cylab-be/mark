package mark.integration;

import java.net.URL;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.client.Client;
import mark.data.DataAgentProfile;
import mark.masfad2.FileSource;
import mark.masfad2.Link;
import mark.masfad2.LinkAdapter;
import mark.server.Config;
import mark.server.Server;

/**
 *
 * @author Thibault Debatty
 */
public class HTTPFileSourceIT extends TestCase {
    private Server server;

    @Override
    protected final void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    public final void testHTTPFileSource() throws Exception {

        System.out.println("Test with a HTTP file source");
        System.out.println("============================");

        Config config = new Config();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Configure a single data source (HTTP, Regex, file with 1000 reqs)
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/1000_http_requests.txt").getPath());

        DataAgentProfile http_1000_source = new DataAgentProfile();
        http_1000_source.class_name = FileSource.class.getCanonicalName();
        http_1000_source.parameters = parameters;
        server.addDataAgentProfile(http_1000_source);

        // Activate the dummy activation profiles
        server.addDetectionAgent(
                DetectionAgentProfile.fromInputStream(
                        getClass()
                                .getResourceAsStream("/detection.readwrite.yml")));


        server.start();

        // Wait for data sources and detection to finish...
        server.awaitTermination();

        // Connect to server
        Client<Link> datastore = new Client<Link>(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());

    }
}
