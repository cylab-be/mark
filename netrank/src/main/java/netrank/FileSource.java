package netrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.data.AbstractDataAgent;
import mark.data.DataAgentProfile;

/**
 *
 * A generic data agent that reads a file as fast as possible, and parse it
 * line by line using a regular expression.
 * This data agent is usually used for testing detection agents or a log file.
 * Hence it does simply print some stats at the end of execution...
 * @author Thibault Debatty
 */
public class FileSource extends AbstractDataAgent {

    private ServerInterface datastore;
    private final String regex =
            "^(\\d{10})\\..*\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s"
            + "(\\S+)\\s(\\S+)\\s(\\S+)\\s(\\S+)\\s.*$";

    private InputStream stream;
    private int line_count;
    private int error_count;

    private volatile boolean run = true;
    private String label;

    /**
     * Return the number of lines in the file that caused an exception while
     * parsing or saving to the datastore.
     * @return
     */
    public final int getErrorCount() {
        return error_count;
    }

    /**
     *
     * @throws Throwable
     */
    @Override
    public void doRun() throws Throwable {

        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line = null;

        try {
            while ((line = in.readLine()) != null) {
                if (!run) {
                    break;
                }

                line_count++;
                try {
                    RawData rd = parse(line);
                    datastore.addRawData(rd);

                } catch (Throwable ex) {
                    error_count++;
                    System.err.println(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(
                    FileSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Print some stats
        System.out.println("----");
        System.out.println("Number of lines: " + line_count);
        System.out.println("Lines with error: " + error_count);
    }

    /**
     * Set the input stream (usually a file) to be used by this data agent.
     * @param stream
     */
    public final void setInputStream(final InputStream stream) {
        this.stream = stream;
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
        data.label = label;
        data.time = Integer.valueOf(match.group(1));
        link.client = match.group(2);

        URI uri = new URI(match.group(6));
        String domain = uri.getHost();
        link.server = domain;
        data.subject = link;

        data.data = line;
        return data;
    }

    public void setDatastore(ServerInterface datastore) {
        this.datastore = datastore;
    }

    public void setProfile(DataAgentProfile profile) {

        File data_file;

        if (profile.path == null) {
            data_file = new File(profile.parameters.get("file"));

        } else {
            File profile_file = new File(profile.path);
            data_file = new File(profile_file.toURI().resolve(profile.parameters.get("file")));
        }

        try {
            this.setInputStream(new FileInputStream(data_file));
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("File does not exist", ex);
        }
        this.label = profile.label;
    }
}
