/*
 * The MIT License
 *
 * Copyright 2017 georgi.
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
package netrank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author Georgi Nikolov
 * Agent tests the ratio of POST bytes sent during the client-server connection.
 * if the ratio exceeds a specified threshold it is tagged as suspicious.
 */
public class Upload implements DetectionAgentInterface<Link> {

    private static final double UPLOAD_THRESHOLD = 0.5;

    /**
     * method for calculating the ratio between the bytes sent via POST method.
     * and the total amount of bytes Sent/Recieved
     * during a client-server connection
     * @param rawdata
     * @return post_bytes_ratio
     */
    private double postBytesSentRatio(final RawData[] raw_data) {
        double post_bytes_ratio = 0;
        long post_bytes = 0;
        long all_bytes = 0;
        // patern for searching for numbers surrounded by whitespace
        Pattern pattern = Pattern.compile("\\s(\\d+)\\s[GET|POST|CONNECT]");
        for (RawData line : raw_data) {

            Matcher matcher = pattern.matcher(line.data);
            if (!matcher.find()) {
                continue;
            }

            long bytes = Integer.parseInt(matcher.group(1));
            // if the metho is post increment the total bytes posted with
            // the value retrieved
            if (line.data.contains(" POST ")) {
                post_bytes += bytes;
            }

            all_bytes += bytes;
        }
        // check that we don't divide by 0
        if (all_bytes != 0) {
            post_bytes_ratio = (double) post_bytes / all_bytes;
        }
        return post_bytes_ratio;
    }

    /**
     * Analyze function inherited from the DetectionAgentInterface.
     * accepts the subject to analyze
     * trigger of the agent
     * the profile used to load the agent
     * the database to which to connect to gather RawData
     * @throws java.lang.Throwable
     */
    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        double post_percentage = postBytesSentRatio(raw_data);

        if (post_percentage > UPLOAD_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = post_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a ratio of POST methods in the "
                    + "connection between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious post ratio of : "
                    + post_percentage + "\n";

            datastore.addEvidence(evidence);
        }
    }
}
