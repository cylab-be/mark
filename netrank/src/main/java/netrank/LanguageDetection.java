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
import java.util.List;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;

/**
 * LanguageDetection class for language detection in email.
 * Note: can be intensive on large collections of email's.
 * @author georgi
 */
public class LanguageDetection implements DetectionAgentInterface<Link> {

    private static final double CONFIDENCE_THRESHOLD = 0.5;
    private static final double RATIO_THRESHOLD = 0.3;

    /**
     * Method for parsing the email data and language detection.
     * Extracts the text from it and running a language detection to
     * determine the language of the text
     * @param text
     * @return List<String> a list of Strings with the different possible
     * language in the body of the email
     * @throws IOException
     */
    private double detectLanguage(final String text) throws IOException {
        double confidence_result = 0;
        MIMEParser parser = new MIMEParser(text);
        String mime_body = parser.getText();
        LanguageDetector detector =
                        new OptimaizeLangDetector().loadModels();
        List<LanguageResult> result = detector.detectAll(mime_body);
        confidence_result = result.get(0).getRawScore();
        return confidence_result;
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

        int susp_lang = 0;
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            //we pass the data field of the Evidence to the detectLanguage
            //method that will get the text body of the email and detect
            //if there is a dominent Language. If the Confidence value of the
            //detected Language is under 0.5, the language used in the body
            //is considered suspicious
            double detect_result = detectLanguage(data);
            if (detect_result < CONFIDENCE_THRESHOLD) {
                susp_lang = susp_lang + 1;
            }
        }

        //We determine the ratio between emails with a suspicious language
        //used in the body and the total amount of emails.
        double lang_percentage = (double) susp_lang / raw_data.length;
        if (lang_percentage > RATIO_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = lang_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found suspicious language emails"
                    + " between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious language ratio of : "
                    + lang_percentage + " between suspicious and normal "
                    + "emails \n";

            datastore.addEvidence(evidence);
        }
    }
}
