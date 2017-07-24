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

import java.io.IOException;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 * The Reputation agent scraps two websites and composes a Reputation Index
 * based on the scores given by those two websites. If the Reputation Index
 * is below a predetermined threshold, evidence is created.
 */
public class Reputation extends WebsiteParser {

    private static final String DEFAULT_URL = "https://www.mywot.com";
    private static final String DEFAULT_PATTERN = "\\r?\\n";
    private static final String DEFAULT_ELEMENT = "div.score-board-"
                                                    + "ratings__index.r1";
    private static final int REPUTATION_THRESHOLD = 50;

/**
 *
 * @param data parameters is the result retrieved from the WebOfTrust search.
 * @param given_pattern is the pattern to use to parse out the data we need.
 * @return returns the reputation that WOT gives the domain in integer
 * WOT has two parameters: "Trustworthiness" and "Child Safety". For this agent
 * we just consider the trustworthiness of the domain.
 */
    @Override
    public final int parse(final String data, final String given_pattern) {
        int reputation = 0;
        if (data != null && !data.isEmpty()) {
            String[] lines = data.split(given_pattern);
            String trustworthiness = lines[0];
            reputation = Integer.parseInt(trustworthiness);
        }
        return reputation;
    }

    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        String domain_name = subject.getServer();
        String reputation = "";
/**
 * returns an estimation if the domain we pass has malicious code
 * attached to it. The "www.mywot.com" website checks the reputation of all IP
 * addresses related to a domain. This reputation is computed by users that
 * submit their information about the domain so its a crowd funded website.
 */
        try {
            String search_url = DEFAULT_URL + "/en/scorecard/" + domain_name;
            reputation = connect(search_url, DEFAULT_ELEMENT);
        } catch (IOException ex) {
            System.out.println("Could not establish connection to server");
            return;
        }

        int parsed_reputation = parse(reputation, DEFAULT_PATTERN);

        if (parsed_reputation < REPUTATION_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a domain:"
                    + " " + domain_name
                    + " " + "with suspiciously low Reputation of "
                    + reputation;
            datastore.addEvidence(evidence);
        }
    }
}
