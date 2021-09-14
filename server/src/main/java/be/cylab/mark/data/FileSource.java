/*
 * The MIT License
 *
 * Copyright 2020 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package be.cylab.mark.data;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import com.google.code.regexp.Pattern;
import com.google.code.regexp.Matcher;
import be.cylab.mark.core.DataAgentInterface;
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import java.util.Map;

/**
 *
 * A generic data agent that reads a file and parse it line by line using a
 * named regular expression. This data agent is usually used for testing
 * detection agents, or to run a demo. This data source takes 2 mandatory
 * parameters:
 * <ul>
 * <li>file : the file to read</li>
 * <li>regex : the named regex to use</li>
 * </ul>
 * @see https://cylab.be/blog/115/mark-use-built-in-file-data-source
 * @author Thibault Debatty
 */
public class FileSource implements DataAgentInterface {

    private static final String FILE_KEY = "file";
    private static final String REGEX_KEY = "regex";

    private static final String SPEED_KEY = "speed";
    private static final double DEFAULT_SPEED = Double.MAX_VALUE;

    private Pattern pattern;
    private volatile boolean must_stop = false;

    /**
     *
     * @param profile
     * @param datastore
     * @throws Throwable if something went wrong...
     */
    @Override
    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {


        double speed = profile.getParameterDouble(SPEED_KEY, DEFAULT_SPEED);
        String file = profile.getParameter(FILE_KEY);
        String regex = profile.getParameter(REGEX_KEY);

        pattern = Pattern.compile(regex);

        int line_count = 0;
        // all times are expressed in milliseconds (except in the parser, see
        // below)
        long start_time = 0;
        long first_data_time = 0;

        File data_file = profile.getPath(file);
        FileInputStream stream = new FileInputStream(data_file);
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line = null;

        while ((line = in.readLine()) != null) {
            if (must_stop) {
                break;
            }

            line_count++;
            try {
                RawData rd = parse(line);
                rd.setLabel(profile.getLabel());

                if (start_time == 0) {
                    start_time = System.currentTimeMillis();
                    first_data_time = rd.getTime();
                }

                // time difference between this record and the first record
                double delta_time = rd.getTime() - first_data_time;
                long simulated_deta_time = (long) (delta_time / speed);
                // Simulated time for this new data
                long simulated_time = start_time + simulated_deta_time;
                rd.setTime(simulated_time);

                long wait_time = simulated_time - System.currentTimeMillis();
                if (wait_time > 0) {
                    Thread.sleep(wait_time);
                }

                datastore.addRawData(rd);
            } catch (Exception ex) {
                System.err.println(
                        "Error: " + ex + " while parsing line: " + line);
            }
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

        if (!match.find()) {
            throw new Exception("Regex did not match line " + line);
        }

        RawData data = new RawData();
        // Parser expects timestamp expressed in seconds in the file
        double time = Double.valueOf(match.group("timestamp")) * 1000.0;
        data.setTime((long) time);

        // The subject consists of all other named groups
        Map<String, String> subject = match.namedGroups().get(0);
        subject.remove("timestamp");
        data.setSubject(subject);

        data.setData(line);
        return data;
    }

    @Override
    public final void stop() {
        this.must_stop = true;
    }
}
