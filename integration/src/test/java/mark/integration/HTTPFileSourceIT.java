package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.activation.InvalidProfileException;
import mark.client.Client;
import mark.masfad2.FileSource;
import mark.masfad2.Link;
import mark.masfad2.LinkAdapter;
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
        System.out.println("============================");
        Server server = new Server();
        server.setSubjectAdapter(new LinkAdapter());

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
                .getResourceAsStream("/detection.readwrite.yml");
        server.addDetectionAgentProfile(
                DetectionAgentProfile.fromInputStream(input));
        server.start();

        // Wait for data sources and detection to finish...
        server.awaitTermination();

        // Connect to server
        Client<Link> datastore = new Client<Link>(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());

        server.stop();
    }
}
