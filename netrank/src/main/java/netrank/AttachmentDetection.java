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

import com.sun.mail.util.BASE64DecoderStream;
import java.io.IOException;
import java.util.ArrayList;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 */
public class AttachmentDetection implements DetectionAgentInterface<Link> {
    private static final double RATIO_THRESHOLD = 0.3;

    private ArrayList getAttachments(final RawData[] raw_data)
            throws IOException {
        ArrayList attachments = new ArrayList();
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            MIMEParser parser = new MIMEParser(data);
            //System.err.println("DEBUG1: " + parser.getAttachment());
            for (int i = 0; i < parser.getAttachment().size(); i++) {
                attachments.add(parser.getAttachment().get(i));
                BASE64DecoderStream stream =
                        (BASE64DecoderStream) parser.getAttachment().get(i);
                //System.err.println("DEBUG2: " + stream.read());
            }
        }
        return attachments;
    }

    @Override
    public final void analyze(final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface<Link> datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);
        ArrayList attachments = getAttachments(raw_data);
        double attachment_data_ratio =
                (double) raw_data.length / attachments.size();
            if (attachment_data_ratio > RATIO_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = attachment_data_ratio;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found emails with suspicious amount"
                    + " of attachments"
                    + " between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious attachment ratio of : "
                    + attachment_data_ratio + " between the amount of"
                    + " attachments and the total amount of emails \n";

            datastore.addEvidence(evidence);
        }
    }
}
