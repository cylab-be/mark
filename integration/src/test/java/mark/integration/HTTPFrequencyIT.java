package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
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
public class HTTPFrequencyIT extends TestCase {

    public final void testFrequencyAgent()
            throws FileNotFoundException, InvalidProfileException {

        /*
        System.out.println("test frequency agent");
        Server masfad_server = new Server();

        // Configure a single data source (HTTP, Regex, file with 47k reqs)
        LinkedList<SourceProfile> sources = new LinkedList<SourceProfile>();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(
                "file",
                getClass().getResource("/47k_http_requests_1client.txt")
                        .getPath());

        SourceProfile http_source = new SourceProfile();
        http_source.class_name = FileSource.class.getCanonicalName();
        http_source.parameters = parameters;

        sources.add(http_source);
        masfad_server.setSourceProfiles(sources);

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/activation-frequency.yml");
        masfad_server.setActivationProfiles(activation_file);
        masfad_server.start();
        masfad_server.stop();*/
    }
}
