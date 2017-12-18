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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import mark.activation.DummyClient;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;
import org.bson.Document;

/**
 *
 * @author georgi
 * @param <T>
 */
public class EmailDummyClient<T extends Subject> extends DummyClient<T> {

    private static final int N = 10000;
    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;
    private LinkedList<Evidence> evidence = new LinkedList<>();
    String email_Mime = "";

    public String parseMIME()
            throws FileNotFoundException, MessagingException, IOException {
        String path = getClass().getResource("/MIME.txt")
                        .getPath();
        Session s = Session.getInstance(new Properties());
        InputStream is = new FileInputStream(path);
        MimeMessage message = new MimeMessage(s, is);
        String field_message_id = "";
        String field_date = "";
        String field_from = "";
        String field_to = "";
        String field_subject = "";
        String field_mime_version = "";
        String field_content_type_text = "";
        String field_content_type_html = "";
        String field_attachment = "";

        message.getAllHeaderLines();
        for (Enumeration<Header> e = message.getAllHeaders();
                e.hasMoreElements();) {
            Header h = e.nextElement();
            switch (h.getName()) {
                case "Message-ID":
                    field_message_id = h.getValue();
                    break;
                case "From":
                    field_from = h.getValue();
                    break;
                case "To":
                    field_to = h.getValue();
                    break;
                case "Subject":
                    field_subject = h.getValue();
                    break;
                case "MIME-Version":
                    field_mime_version = h.getValue();
                    break;
                case "Date":
                    field_date = h.getValue();
                    break;
                default:
                    break;
            }
        }

        Multipart mp = (Multipart) message.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart bp = mp.getBodyPart(i);
            if (bp.isMimeType("text/plain")) {
                field_content_type_text = (String) bp.getContent();
            } else if (bp.isMimeType("multipart/*")) {
                Multipart submp = (Multipart) bp.getContent();
                for (int n = 0; n < submp.getCount(); n++) {
                    BodyPart subbp = submp.getBodyPart(n);
                    if (subbp.isMimeType("text/html")) {
                        field_content_type_html = (String) subbp.getContent();
                    }
                }
            }
        }
        
        String output = "Message-ID=" + field_message_id + " , "
                + "Date=" + field_date + " , "
                + "From=" + field_from + " , "
                + "To=" + field_to + " , "
                + "Subject=" + field_subject + " , "
                + "MIME-Version=" + field_mime_version + " , "
                + "Text=" + field_content_type_text + " , "
                + "HTML=" + field_content_type_html + " , "
                + "Attachments=" + field_attachment + " , ";
        return output;
        
//        FileReader file = new FileReader(path);
//        BufferedReader reader = new BufferedReader(file);
//        String line = reader.readLine();
//
//        while(line != null) {
//            email_Mime += line;
//            line = reader.readLine();
//        }
//        return email_Mime;
    }

    private RawData[] generateData(String type, Link subject)
            throws FileNotFoundException, MessagingException, IOException {

        RawData[] data = new RawData[N];
        int start = 123456;
        Random rand = new Random();

        // Normal Traffic
        for (int i = 0; i < N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(5 * APT_INTERVAL);
            data[i].data = parseMIME();
        }
        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] result = generateData(type, (Link) subject);
        return result;
    }

    @Override
    public RawData[] findData(Document query)
            throws Throwable {

        RawData[] data = generateData("data.smtp",
                 new Link("192.168.2.3", " "));
        return data;
    }

    @Override
    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
        this.evidence.add(evidence);
    }

    @Override
    public Evidence[] findEvidence(String label) {
        return new Evidence[1];
    }

    public LinkedList<Evidence> getEvidences() throws Throwable {
        return this.evidence;
    }
    
}
