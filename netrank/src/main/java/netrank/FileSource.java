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
 * A generic data agent that reads a file and parse it line by line using a
 * regular expression.
 * This data agent is usually used for testing detection agents, or to run a
 * demo.
 * @author Thibault Debatty
 */
public class FileSource implements DataAgentInterface {

    /**
     * Use this key in the parameters map to set the speedup for reading the
     * file.
     */
    public static final String SPEEDUP_KEY = "speedup";

    private static final double DEFAULT_SPEEDUP = Double.MAX_VALUE;

    private final String regex =
            "^(\\d{10})\\..*\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s"
            + "(\\S+)\\s(\\S+)\\s(\\S+)\\s(\\S+)\\s.*$";
    private Pattern pattern;

    /**
     *
     * @param profile
     * @param datastore
     * @throws Throwable if something went wrong...
     */
    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {

        int line_count = 0;

        double speedup = DEFAULT_SPEEDUP;
        String speedup_string = profile.parameters.get(SPEEDUP_KEY);
        if (speedup_string != null) {
            try {
                speedup = Double.valueOf(speedup_string);
            } catch (NumberFormatException ex) {
                speedup = DEFAULT_SPEEDUP;
            }
        }

        long start_time = 0;
        long first_data_time = 0;

        pattern = Pattern.compile(regex);

        File data_file = profile.getPath(profile.parameters.get("file"));
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

            if (start_time == 0) {
                start_time = System.currentTimeMillis();
                first_data_time = rd.time;
            }

            // Simulated time for this new data
            rd.time = rd.time - first_data_time + start_time;

            long wait_time = (long) ((rd.time - start_time) / speedup);
            Thread.sleep(wait_time);

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

        Matcher match = pattern.matcher(line);

        if (!match.matches()) {
            throw new Exception("Regex did not match line " + line);
        }



        RawData data = new RawData();
        data.time = Integer.valueOf(match.group(1));
        data.subject = new Link(
                match.group(2),
                new URI(match.group(6)).getHost());
        data.data = line;
        return data;
    }
}
