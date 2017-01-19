package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.activation.InvalidProfileException;
import mark.masfad2.FileSource;
import mark.masfad2.LinkAdapter;
import mark.server.Server;
import mark.server.DataAgentProfile;

/**
 *
 * @author Thibault Debatty
 */
public class HTTPFrequencyIT extends TestCase {

    public final void testFrequencyAgent()
            throws FileNotFoundException, InvalidProfileException, MalformedURLException, Exception {


        System.out.println("test frequency agent");
        System.out.println("====================");

        Server masfad_server = new Server();
        masfad_server.setSubjectAdapter(new LinkAdapter());

        // Configure a single data source (HTTP, Regex, file with 47k reqs)
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/47k_http_requests_1client.txt")
                        .getPath());

        DataAgentProfile http_source = new DataAgentProfile();
        http_source.class_name = FileSource.class.getCanonicalName();
        http_source.parameters = parameters;

        masfad_server.addDataAgentProfile(http_source);

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/detection.http.frequency.yml");
        masfad_server.addDetectionAgentProfile(DetectionAgentProfile.fromInputStream(activation_file));
        masfad_server.start();
        masfad_server.stop();
    }
}
