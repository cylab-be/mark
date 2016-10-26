package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import junit.framework.TestCase;
import mark.activation.InvalidProfileException;
import mark.agent.data.regex.FileSource;
import mark.server.Server;
import mark.server.SourceProfile;

/**
 *
 * @author Thibault Debatty
 */
public class HTTPFileSourceIT extends TestCase {

    public final void testHTTPFileSource()
            throws FileNotFoundException, InvalidProfileException,
            MalformedURLException {

        System.out.println("test with a HTTP file source");
        Server masfad_server = new Server();

        // Configure a single data source (HTTP, Regex, file with 1000 reqs)
        LinkedList<SourceProfile> sources = new LinkedList<SourceProfile>();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/1000_http_requests.txt").getPath());

        SourceProfile http_1000_source = new SourceProfile();
        http_1000_source.class_name = FileSource.class.getCanonicalName();
        http_1000_source.parameters = parameters;

        sources.add(http_1000_source);
        masfad_server.setSourceProfiles(sources);

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/activation-dummy.yml");
        masfad_server.setActivationProfiles(activation_file);
        masfad_server.start();
        masfad_server.stop();
    }
}
