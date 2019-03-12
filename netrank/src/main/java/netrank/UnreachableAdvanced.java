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

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Agent responsible for analyzing the connection status to a server.
 * Checks if in the same time period as the bad connections other unreachable
 * connections are present to other servers, determining if its the clients
 * machine responsible for the bad connection or suspicious activity is present
 * @author georgi
 */
public class UnreachableAdvanced implements DetectionAgentInterface<Link> {

    private final int[] bad_server_status = new int[] {500, 501, 502, 503, 504};
    private static final double DEFAULT_UNREACHABLE_PERIODICITY = 0.02;
    //Private function to determine if a status shows a bad connection
    //to the server
    private boolean doesContain(final int status) {
        boolean result = false;
        for (int i = 0; i < bad_server_status.length; i++) {
            if (status == bad_server_status[i]) {
                result = true;
            }
        }
        return result;
    }

    private double checkPeriodicity(final int[] values) {
        //Check the percentage of unreachable connections
        int nmb_unreachable_connections = 0;
        int percentage_unreachable_connections = 0;
        double result = 1;
        for (int n = 0; n < values.length; n++) {
            int status = values[n];
            if (doesContain(status)) {
                nmb_unreachable_connections = nmb_unreachable_connections + 1;
            }
        }

        if (nmb_unreachable_connections == 0) {
            return 0;
        } else {
            percentage_unreachable_connections = 1
                    - (nmb_unreachable_connections / values.length);
        }
        //If more than half the connections are unreachable check how the
        //connection statuses follow each other
        if (percentage_unreachable_connections > 0.4) {

        //Check how the statuses follow each other, is it a constant switch
        //between a reachable and unreachable status or a large time periods
        //with connection to the server followed by an unreachable period

            int previous_status = 0;
            for (int i = 0; i < values.length; i++) {
                int status = values[i];
                if (previous_status == 0) {
                    previous_status = status;
                } else {
                    if (previous_status == status) {
                        result = result - 0.1;
                    }
                }
                previous_status = status;
            }
        }
        if (result < 0) {
            result = 0;
        }
        return result;
    }

    // Analyze function inherited from the DetectionAgentInterface
    // accepts the subject to analyze
    // trigger of the agent
    // the profile used to load the agent
    // the database to which to connect to gather RawData
    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 50) {
            System.out.println("No Raw Data Given \n");
            return;
        }

        long[][] times_status = new long[raw_data.length][2];
        int[] status_array = new int[raw_data.length];
        Pattern pattern = Pattern.compile(".*TCP_MISS/([0-9]{3}).*");
        for (int i = 0; i < raw_data.length; i++) {
            long timestamp = raw_data[i].time;
            int status = 0;
            Matcher matcher = pattern.matcher(raw_data[i].data);
            if (matcher.find()) {
                status = Integer.parseInt(matcher.group(1));
            }
            long[] time_status = {timestamp, status};
            times_status[i] = time_status;
            status_array[i] = status;
        }

        double unreachable_periodicity = checkPeriodicity(status_array);

        if (unreachable_periodicity > DEFAULT_UNREACHABLE_PERIODICITY) {
            Evidence evidence = new Evidence();
            evidence.score = unreachable_periodicity;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found an unreachable periodicity in"
                    + " the connection between:"
                    + "<br /> client " + subject.getClient()
                    + " and server " + subject.getServer()
                    + " with unreachable periodicity: "
                    + unreachable_periodicity
                    + "<br />Number of entries analysed: " + raw_data.length
                    + "<br />The Unreachable threshold ratio used is: "
                    + DEFAULT_UNREACHABLE_PERIODICITY;

            datastore.addEvidence(evidence);
        }
    }
}
