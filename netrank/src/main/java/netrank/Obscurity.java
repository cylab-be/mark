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
 * @author Georgi Nikolov
 * The Obscurity agent uses the Bing API to execute a search of the domain.
 * Afterwards we look at the number of results produced and if the quantity is
 * under a predetermined threshold its considered suspicious as it gives insight
 * in how obscure the domain is.
 */
public class Obscurity extends WebsiteParser {

    private static final String DEFAULT_URL = "https://www.bing.com/search?q="
                                                + "####"
                                                + "&setlang=en-gb";
    private static final String DEFAULT_PATTERN = "(.*?) (?i)r";
    private static final String DEFAULT_ELEMENT = "span.sb_count";
    private static final int OBSCURITY_THRESHOLD = 1000;


    @Override
    public final int parse(final String data, final String given_pattern) {
        int result = 0;
        //use regex pattern to extract the numbers from the result string.
        Pattern pattern = Pattern.compile(given_pattern);
        Matcher matcher = pattern.matcher(data);
        //if a pattern is found replace the symbols delimiting the numbers
        //with nothing so we can transform the String numbers to Integer.
        String matched_string = "";
        if (matcher.find()) {
            if (matcher.group(1).contains("&nbsp;")) {
                matched_string = matcher.group(1).replaceAll("&nbsp;", "");
            } else if (matcher.group(1).contains(",")) {
                matched_string = matcher.group(1).replaceAll(",", "");
            } else {
                matched_string = matcher.group(1);
            }
            result = Integer.parseInt(matched_string);
        }
        return result;
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
        String number_of_results = "";
        String search_url = "";
        String element_to_search_for = DEFAULT_ELEMENT;
        String pattern_to_search_for = DEFAULT_PATTERN;

        //check if the parameter field in the config file has been filled
        //if it is adapt the DEFAULT parameters with those from the config file.
        if (profile.parameters != null) {
            search_url = profile.parameters.values().toArray()[0].toString()
                                                .replace("####", domain_name);
            element_to_search_for = profile.parameters.values().toArray()[2]
                                                .toString();
            pattern_to_search_for = profile.parameters.values().toArray()[1]
                                                .toString();
        } else {
            search_url = DEFAULT_URL.replace("####", domain_name);
        }

        try {
            number_of_results = connect(search_url, element_to_search_for);
        } catch (IOException ex) {
            System.out.println("Could not establish connection to server");
            return;
        }
        //System.out.println("NUMBERS: " + number_of_results);

        int parsed_results = parse(number_of_results, pattern_to_search_for);


        if (parsed_results < OBSCURITY_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a domain:"
                    + " " + domain_name
                    + " that has suspiciosly low Obscurity value."
                    + parsed_results + " references " + "found of "
                    + domain_name
                    + " on search engines.";
            datastore.addEvidence(evidence);
        }
    }
}
