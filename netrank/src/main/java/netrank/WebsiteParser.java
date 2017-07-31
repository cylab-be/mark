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
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.ServerInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author georgi
 */
public abstract class WebsiteParser implements DetectionAgentInterface<Link> {

    private static final String SEARCH_AGENT = "Mozilla/5.0 "
            + "(Windows NT 6.2; WOW64) AppleWebKit/537.15 "
            + "(KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15";

    /**
     *
     * @param data is the data we are going to parse.
     * @param given_pattern the pattern used to extract information from the
     * data.
     * @return returns the score extracted from the data.
     */
    public abstract int parse(final String data, final String given_pattern);
    //if a pattern is found replace the symbols delimiting the numbers
    //with nothing so we can transform the String numbers to Integer.

    /**
     *
     * @param formattedwebsite the website we are trying to parse for data.
     * @param element the HTML element we are interested in on the website.
     * @return returns the HTML document in String format.
     * @throws IOException if not connection to website.
     */
    public final String connect(final String formattedwebsite,
                        final String element) throws IOException {
        String result = "";
        String search_url = formattedwebsite;
        Document doc = Jsoup.connect(search_url).timeout(5000)
                .userAgent(SEARCH_AGENT).get();

        //search for the span DOM element that holds the # of results
        System.out.println("DOCUMENT:" + doc.html());
        Elements result_element = doc.select(element);
        result = result_element.html();
        return result;
    }

    @Override
    public abstract void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable;
}
