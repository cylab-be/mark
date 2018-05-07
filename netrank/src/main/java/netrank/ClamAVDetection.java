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

import fi.solita.clamav.ClamAVClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
//import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 */
public class ClamAVDetection implements DetectionAgentInterface<Link> {
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 3310;

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

        String server = "";
        int port = 3310;
        //check if the parameter field in the config file has been filled
        //if it is adapt the DEFAULT parameters with those from the config file.
        if (profile.parameters != null) {
            server = profile.parameters.values().toArray()[0].toString();
            port = (int) profile.parameters.values().toArray()[1];
        } else {
            server = DEFAULT_SERVER;
        }
        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);
        //Get all the attachments from the raw data
        ArrayList attachments = getAttachments(raw_data);
        //Extract all the unique attachments from the collection of attachments
        Set<String> unique_attachments = new HashSet<>(attachments);

        //Connect to CLAMAV to scan the attachments
        // Create a ClamAvScanned with a given hostname/IPAddress and the port.
        // This should match what is put in the /etc/clamav/clamd.conf file.
        // The third value is the timeout in milliseconds before we give up
        // on talking to the ClamAV daemon
        // If the Daemon is running on the same machine,
        //you can specify "localhost" as the IP address.
        ClamAVClient cl = new ClamAVClient(server, port, 500000);
        byte[] reply;
        for (String s: unique_attachments) {
            try {
              reply = cl.scan(new ByteArrayInputStream(s.getBytes()));
            } catch (IOException e) {
              throw new RuntimeException("Could not scan the input", e);
            }
            if (ClamAVClient.isCleanReply(reply)) {
                continue;
            } else {
                Evidence evidence = new Evidence();
                evidence.score = 1;
                evidence.subject = subject;
                evidence.label = profile.label;
                evidence.time = raw_data[raw_data.length - 1].time;
                evidence.report = "Found emails with suspicious "
                        + "attachments that contain malicious code"
                        + " between " + subject.getClient()
                        + " and " + subject.getServer() + "\n";
                datastore.addEvidence(evidence);
            }
        }
    }
}
