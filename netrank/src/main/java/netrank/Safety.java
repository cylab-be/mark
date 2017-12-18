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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 * The Safety Reputation agent scraps URLVOID website which tests the given
 * domain with different online tools to detect threats.
 */
public class Safety extends WebsiteParser {

    private static final String DEFAULT_URL = "http://www.urlvoid.com/scan/";
    private static final String DEFAULT_PATTERN = "^(\\d+)/";
    private static final String DEFAULT_ELEMENT = "span.label.label-danger";
    private static final int SAFETY_THRESHOLD = 7;

    @Override
    public final int parse(final String data, final String given_pattern) {
        //set the default safety vaule above the threshold.
        //if the domain is unknown it wont return a value so it will be
        //considered unsafe.
        int parsed_int = 8;
        if (data != null && !data.isEmpty()) {
            Pattern pattern = Pattern.compile(given_pattern);
            Matcher matcher = pattern.matcher(data);
            //if a pattern is found replace the symbols delimiting the numbers
            //with nothing so we can transform the String numbers to Integer.
            if (matcher.find()) {
                parsed_int = Integer.parseInt(matcher.group(1));
            }
        }
        return parsed_int;
    }

    /**
     * Analyze function inherited from the DetectionAgentInterface.
     * accepts the subject to analyze
     * trigger of the agent
     * the profile used to load the agent
     * the database to which to connect to gather RawData
     * @param subject
     * @param actual_trigger_label
     * @param profile
     * @param datastore
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

        String domain_name = subject.getServer();
        String safety_reputation = "";
///**
// * returns the Safety Reputation given by URLVOID. THe safety reputation
// * is between 0 and 34 where each point means one of the 34 detector agents
// * have discovered something malicious in the domain. A safety reputation
// * of 0 is good, anything above is suspicious.
// * Note that a 0 might also mean that the domain wasn't found by URLVOID =
// * uknown domain.
// */
        try {
            String search_url = DEFAULT_URL + domain_name + "/";
            safety_reputation = connect(search_url, DEFAULT_ELEMENT);
        } catch (IOException ex) {
            System.out.println("Could not establish connection to server");
            return;
        }

        int parsed_reputation = parse(safety_reputation, DEFAULT_PATTERN);

        if (parsed_reputation > SAFETY_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a domain:"
                    + " " + domain_name
                    + " that has suspiciosly low Safety Reputation value."
                    + " http://www.urlvoid.com has found that the domain"
                    + " has a very low Safety Reputation (or no data on the"
                    + "domain), of "
                    + parsed_reputation + "/34 " + "where " + parsed_reputation
                    + " detectors out of the 34 found something suspicious.";
            datastore.addEvidence(evidence);
        }
    }
}
