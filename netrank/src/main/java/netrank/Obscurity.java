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
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Georgi Nikolov
 * The Obscurity agent uses the Bing API to execute a search of the domain.
 * Afterwards we look at the number of results produced and if the quantity is
 * under a predetermined threshold its considered suspicious as it gives insight
 * in how obscure the domain is.
 */
public class Obscurity implements DetectionAgentInterface<Link> {

    private static final String BING_SEARCH_URL = "https://www.bing.com/search";
    private static final String BING_SEARCH_AGENT = "Mozilla/5.0 "
            + "(Windows NT 6.2; WOW64) AppleWebKit/537.15 "
            + "(KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15";
    private static final int OBSCURITY_THRESHOLD = 1000;

    private String extractResultNumber(final String string) {
        String result = "";
        Pattern pattern = Pattern.compile("(.*?) (?i)r");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            if (matcher.group(1).contains("&nbsp;")) {
                result = matcher.group(1).replaceAll("&nbsp;", "");
            } else if (matcher.group(1).contains(",")) {
                result = matcher.group(1).replaceAll(",", "");
            } else if (matcher.group(1).contains(".")) {
                result = matcher.group(1).replaceAll(".", "");
            } else {
                result = matcher.group(1);
            }
        }
        return result;
    }

    private int connectToBing(final String word) throws IOException {
        String search_url = BING_SEARCH_URL + "?q=" + word;
        Document doc = Jsoup.connect(search_url)
                .userAgent(BING_SEARCH_AGENT).get();

        //Elements result = doc.select("li.b_algo h2 a");

        //search for the span DOM element that holds the # of results
        Elements result_element = doc.select("span.sb_count");
        //extract the number and transform it to int from String
        System.out.println(result_element.html());
        String results = extractResultNumber(result_element.html());
        if (results.equals("")) {
            return 0;
        }
        int number_of_results = Integer.parseInt(results);
        return number_of_results;

        //code for iterating over the results found in the search
//        for (Element res : result) {
//            String linkHref = res.attr("href");
//            System.out.println("<a href=" + linkHref + ">"
//                                                      + linkHref + "</a>");
//            counter = counter + 1;
//        }
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

        int number_of_results = connectToBing(domain_name);

        if (number_of_results < OBSCURITY_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a domain:"
                    + " " + domain_name
                    + " that has suspiciosly low Obscurity value."
                    + number_of_results + " references " + "found of "
                    + domain_name
                    + " on search engines.";
            datastore.addEvidence(evidence);
        }
    }
}
