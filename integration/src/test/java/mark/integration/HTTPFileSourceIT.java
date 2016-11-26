package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.activation.InvalidProfileException;
import mark.agent.data.regex.FileSource;
import mark.server.DataAgentProfile;
import mark.server.Server;

/**
 *
 * @author Thibault Debatty
 */
public class HTTPFileSourceIT extends TestCase {

    public final void testHTTPFileSource()
            throws FileNotFoundException, InvalidProfileException,
            MalformedURLException, Exception {

        System.out.println("Test with a HTTP file source");
        Server server = new Server();

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
        InputStream input = getClass()
                .getResourceAsStream("/detection.dummy.yml");
        server.addDetectionAgentProfile(DetectionAgentProfile.fromInputStream(input));
        server.start();
        server.stop();
    }
}
