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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.apache.commons.net.whois.WhoisClient;

/**
 *
 * @author Georgi Nikolov
 * this agent determines the age of the server domain accessed by the client.
 * If the server domain is younger than a specific threshold it is considered
 * suspicious and evidence is produced.
 */
public class DomainAge implements DetectionAgentInterface<Link> {

    /**
     * creating the WHOIS client. To be initialized in the correct method
    */
    private static final String CREATION_DATE = "   Creation Date";
    private static final String EXPIRATION_DATE = "   Expiration Date";
    private static final String UPDATED_DATE = "   Updated Date";
    private static final long TIME_THRESHOLD = 30;

    /**
     * initiate the WHOIS client.
     */
    private WhoisClient initWhois() throws IOException {
        WhoisClient whois_client = new WhoisClient();

        whois_client.setDefaultTimeout(10000);
        whois_client.setConnectTimeout(5000);
        whois_client.connect(WhoisClient.DEFAULT_HOST);

        return whois_client;
    }

    /**
     * check if a string is an ip or text.
     */
    private static boolean isIp(final String text) {
        Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]"
                        + "|[01]?[0-9][0-9]?)"
                        + "\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher m = p.matcher(text);
        return m.find();
    }

    /**
     * check if the hostname end with ".com"/".net"/".edu".
     */
    private static boolean validHostname(final String text) {
        if (text.endsWith(".com") || text.endsWith(".net")
                || text.endsWith(".edu")) {
            return true;
        }
        return false;
    }

    /**
     * method for extracting attributes from the whois data.
     *
     * @param whois_data
     * @return
     */
    private Map<String, String> getAttributes(final String whois_data) {
        Pattern p = Pattern.compile("^(.*?): (.*)$");
        Map<String, String> attributes = new HashMap<>();
        Scanner s = new Scanner(whois_data);
        while (s.hasNextLine()) {
            Matcher m = p.matcher(s.nextLine());
            if (m.matches()) {
            attributes.put(m.group(1), m.group(2));
            }
        }
        s.close();
        return attributes;
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

        String domain_name = subject.getServer();

        if (!validHostname(domain_name) && !isIp(domain_name)) {
           System.out.println("The Registry database contains ONLY .COM, .NET,"
                   + " .EDU domains names." + "\n");
           return;
        }

        WhoisClient whois_client;
        try {
            whois_client = initWhois();
        } catch (IOException ex) {
            System.out.println("Could not establish connection to server");
            return;
        }

        String whois_result = whois_client.query("=" + domain_name);
        whois_client.disconnect();

        if (whois_result.contains("No match for ")) {
            System.out.println("No data found about the server" + "\n");
            return;
        }

        Map<String, String> attributes = getAttributes(whois_result);
        String creation_date = attributes.get(CREATION_DATE);
        //String updated_date = attributes.get(UPDATED_DATE);
        //String expiration_date = attributes.get(EXPIRATION_DATE);
        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date formated_creation_date = format.parse(creation_date);
        Date current_date = new Date();
        long time_difference = TimeUnit.MILLISECONDS.toDays(
                current_date.getTime() - formated_creation_date.getTime());

            if (time_difference < TIME_THRESHOLD) {
                Evidence evidence = new Evidence();
                evidence.score = 1;
                evidence.subject = subject;
                evidence.label = profile.label;
                evidence.time = raw_data[raw_data.length - 1].time;
                evidence.report = "Found a domain:"
                        + " " + domain_name
                        + " that was created less than a month ago."
                        + " Created: " + time_difference + " days ago.";
                datastore.addEvidence(evidence);
            }
    }
}
