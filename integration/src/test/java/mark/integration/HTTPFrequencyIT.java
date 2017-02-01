package mark.integration;

import java.util.HashMap;
import junit.framework.TestCase;
import mark.core.DetectionAgentProfile;
import netrank.FileSource;
import netrank.LinkAdapter;
import mark.server.Config;
import mark.server.Server;
import mark.core.DataAgentProfile;

/**
 *
 * @author Thibault Debatty
 */
public class HTTPFrequencyIT extends TestCase {
    private Server server;

    @Override
    protected final void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    public final void testFrequencyAgent()
            throws Throwable {


        System.out.println("test frequency agent");
        System.out.println("====================");

        Config config = Config.getTestConfig();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Configure a single data source (HTTP, Regex, file with 2k reqs)
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/2k_http_requests_1client.txt")
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
                        .getResourceAsStream("/detection.http.frequency.yml")));
        server.start();
        server.awaitTermination();
    }
}
