package mark.integration;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.server.InvalidProfileException;
import mark.masfad2.FileSource;
import mark.masfad2.LinkAdapter;
import mark.server.Config;
import mark.server.Server;
import mark.data.DataAgentProfile;

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
            throws FileNotFoundException, InvalidProfileException, MalformedURLException, Exception {


        System.out.println("test frequency agent");
        System.out.println("====================");

        Config config = new Config();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Configure a single data source (HTTP, Regex, file with 47k reqs)
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/47k_http_requests_1client.txt")
                        .getPath());

        DataAgentProfile http_source = new DataAgentProfile();
        http_source.class_name = FileSource.class.getCanonicalName();
        http_source.parameters = parameters;

        server.addDataAgentProfile(http_source);

        // Activate the dummy activation profiles
        server.addDetectionAgent(
                DetectionAgentProfile.fromInputStream(
                        getClass()
                        .getResourceAsStream("/detection.http.frequency.yml")));
        server.start();
        server.awaitTermination();
    }
}
