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
import java.util.HashSet;
import java.util.Set;
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
    private static final double RATIO_UNIQUE_ATTACHMENTS = 0.5;

    private ArrayList getAttachments(final RawData[] raw_data)
            throws IOException {
        ArrayList attachments = new ArrayList();
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            MIMEParser parser = new MIMEParser(data);
            for (int i = 0; i < parser.getAttachment().size(); i++) {
                attachments.add(parser.getAttachment().get(i).toString());
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
        //Get all the attachments from the raw data
        ArrayList attachments = getAttachments(raw_data);
        //Extract all the unique attachments from the collection of attachments
        Set<String> unique_attachments = new HashSet<>(attachments);
        double unique_attachments_ratio =
                unique_attachments.size() / (double) attachments.size();
        double attachment_data_ratio =
                attachments.size() / (double) raw_data.length;
        if (unique_attachments_ratio > RATIO_UNIQUE_ATTACHMENTS) {
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
}
