/*
 * The MIT License
 *
 * Copyright 2019 georgi.
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.bson.Document;

/**
 * The UnreachableCompare agent is triggered when the Unreachable agent produces
 * evidences for a specific subject. The UnreachableCompare agent will then get
 * all raw data for the client in the subject and compare if other unreachable
 * connections were present at the time when the Unreachable agent detected some
 * in the connection described in the subject. TDone to check if the problem of
 * unreachable connection stems from the client or if the server is suspicious
 * @author georgi
 */
public class UnreachableCompare implements DetectionAgentInterface<Link> {

    private final int[] bad_server_status = {500, 501, 502, 503, 504};
    //default label to search for data in the RawData part of the database
    //for comparison with the evidence
    private static final String DEFAULT_LABEL = "data.http";
    private static final String LABEL_STRING = "datalabel";

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

    private boolean detectedSimultaneousUnreachable(final RawData[] raw_data,
                                                    final Link subject) {
        boolean result = false;
        int number_simultaneous_unreachable = 0;
        List<Long> timestamps = new ArrayList<>();
        Pattern pattern = Pattern.compile("/" + "([0-9]{3})\\s");
        //iterate through the data and extract all the timestamps where
        //unreachable connection occured
        for (RawData rawdata: raw_data) {
            String sub = rawdata.subject.toString();
            int status = 0;
            if (sub.equals(subject.toString())) {
                Matcher matcher = pattern.matcher(rawdata.data);
                if (matcher.find()) {
                    status = Integer.parseInt(matcher.group(1));
                }
                if (doesContain(status)) {
                    timestamps.add(rawdata.time);
                }
            }
        }
        //iterate again through the data and compare all other connections if
        //they have unreachable status at the same time as the original subject
        for (RawData rawdata: raw_data) {
            String sub = rawdata.subject.toString();
            int status = 0;
            //if we fall on rawdata from the original subject we just ingore it
            //as this connection is what we compare with
            if (!sub.equals(subject.toString())) {
                long rawdata_time = rawdata.time;
                //iterate over the timestamps when unreachable status occured
                for (long time: timestamps) {
                    //check if the timestamp of the current rawdata analysed is
                    //in the range (+- 5sec) of when unreachable status was
                    //detected and if itself is unreachable status
                    long lower_limit = time - 5000;
                    long upper_limit = time + 5000;
                    if (rawdata_time >= lower_limit
                            && rawdata_time <= upper_limit) {
                        //check if unreachable status occured
                        Matcher matcher = pattern.matcher(rawdata.data);
                        if (matcher.find()) {
                            status = Integer.parseInt(matcher.group(1));
                        }
                        if (doesContain(status)) {
                            result = true;
                            number_simultaneous_unreachable =
                                    number_simultaneous_unreachable + 1;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface<Link> datastore) throws Throwable {

        //check for parameters set through the config file
        String label_to_use = DEFAULT_LABEL;
        String config_label = profile.parameters.get(LABEL_STRING);
        if (config_label != null) {
            try {
                label_to_use = config_label;
            } catch (NumberFormatException ex) {
                label_to_use = DEFAULT_LABEL;
            }
        }
        //get the CLIENT and SERVER for which the Unreachable agent produced
        //evidence and compare them to other connections from this CLIENT
        String client = LinkAdapter.CLIENT;
        String server = LinkAdapter.SERVER;
        Document query = new Document(client, subject.getClient())
                .append("LABEL", label_to_use);
        RawData[] raw_data = datastore.findData(query);

        boolean detected_simultaneous =
                detectedSimultaneousUnreachable(raw_data, subject);

        if (!detected_simultaneous) {

            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found connection between: "
                    + "<br /> client " + subject.getClient()
                    + " and server " + subject.getServer()
                    + "<br />where the number of times the server was "
                    + "unreachable is above the allowed threshold and"
                    + "<br />where no other Unreachable"
                    + " connections were present"
                    + " at the same time as the unreachable connection between"
                    + " " + subject.getClient() + " and "
                    + subject.getServer()
                    + "<br />Number of entries analysed: " + raw_data.length
                    + "\n";

            datastore.addEvidence(evidence);
        }
    }
}
