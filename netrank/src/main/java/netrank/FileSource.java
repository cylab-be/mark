package netrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DataAgentInterface;
import mark.core.DataAgentProfile;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * A generic data agent that reads a file as fast as possible, and parse it
 * line by line using a regular expression.
 * This data agent is usually used for testing detection agents or a log file.
 * Hence it does simply print some stats at the end of execution...
 * @author Thibault Debatty
 */
public class FileSource implements DataAgentInterface {

    private final String regex =
            "^(\\d{10})\\..*\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s"
            + "(\\S+)\\s(\\S+)\\s(\\S+)\\s(\\S+)\\s.*$";


    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {

        int line_count = 0;

        File data_file = new File(profile.parameters.get("file"));
        if (profile.path != null) {
            File profile_file = new File(profile.path);
            data_file = new File(profile_file.toURI().resolve(
                    profile.parameters.get("file")));
        }

        FileInputStream stream = new FileInputStream(data_file);
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line = null;

        while ((line = in.readLine()) != null) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }

            line_count++;
            RawData rd = parse(line);
            rd.label = profile.label;
            datastore.addRawData(rd);
        }

        // Print some stats
        System.out.println("----");
        System.out.println("Number of lines: " + line_count);
    }

    /**
     *
     * @param line
     * @return
     * @throws Exception
     */
    protected final RawData parse(final String line) throws Exception {
        Pattern p = Pattern.compile(regex);
        Matcher match = p.matcher(line);

        if (!match.matches()) {
            throw new Exception("Regex did not match line " + line);
        }

        Link link = new Link();

        RawData data = new RawData();
        data.time = Integer.valueOf(match.group(1));
        link.client = match.group(2);

        URI uri = new URI(match.group(6));
        String domain = uri.getHost();
        link.server = domain;
        data.subject = link;

        data.data = line;
        return data;
    }
}
