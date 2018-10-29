/*
 * The MIT License
 *
 * Copyright 2018 georgi.
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 * HyperlinkDetection class for detection of hyperlinks in emails.
 * The detector will look for hyperlinks in the body of the email and return an
 * evidence if hyperlinks were found.
 * @author georgi
 */
public class HyperlinkDetection implements DetectionAgentInterface<Link> {

    private static final double HYPERLINK_THRESHOLD = 0.5;
    private static final String REGEX = "(?:(?:https?|ftp):\\/\\/|\\b(?:"
                    + "[a-z\\d]+\\.))(?:(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|(?:\\("
                    + "[^\\s()<>]+\\)))?\\))+(?:\\((?:[^\\s()<>]+|(?:\\(?:[^\\s"
                    + "()<>]+\\)))?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))?";

    private int detectHyperlinks(final RawData[] raw_data)
                        throws IOException {
        int hyperlink_amount = 0;
        ArrayList hyperlinks = new ArrayList();
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            MIMEParser parser = new MIMEParser(data);
            String body = parser.getText();
            String html = parser.getHTML();
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                hyperlinks.add(matcher.group());
            }
        }
        hyperlink_amount = hyperlinks.size();
        return hyperlink_amount;
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
            final ServerInterface<Link> datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        if (raw_data.length < 50) {
            return;
        }

        int hyperlink_amount = detectHyperlinks(raw_data);
        double email_hyperlink_ratio =
                    hyperlink_amount / (double) raw_data.length;
        if (email_hyperlink_ratio > HYPERLINK_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = email_hyperlink_ratio;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found emails with suspicious amount"
                    + " of hyperlinks"
                    + " between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious hyperlink ratio of : "
                    + email_hyperlink_ratio + " between the amount of"
                    + " hyperlinks and the total amount of emails \n";
            datastore.addEvidence(evidence);
            }
    }
}
