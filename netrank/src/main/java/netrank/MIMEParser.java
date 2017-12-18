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
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Class responsible for parsing an e-mail in MIME format to an object which
 * fields can be easily accessed via get/set.
 * @author georgi
 */
public class MIMEParser {

    private final InputStream mime_path;
    private String field_message_id;
    private String field_date;
    private String field_from;
    private String field_to;
    private String field_subject;
    private String field_mime_version;
    private final String field_content_type_text;
    private final String field_content_type_html;
    private final String field_attachment;

    /**
     *
     * @param email_path
     */
    public MIMEParser(final InputStream email_path) {
        this.mime_path = email_path;
        try {
            parseMimeEmail();
        } catch (MessagingException | FileNotFoundException ex) {
            System.out.println("Error Parsing MIME Email. Error: " + ex);
        }
        this.field_message_id = "";
        this.field_date = "";
        this.field_from = "";
        this.field_to = "";
        this.field_subject = "";
        this.field_mime_version = "";
        this.field_content_type_text = "";
        this.field_content_type_html = "";
        this.field_attachment = "";
    }

    private void parseMimeEmail() throws MessagingException,
            FileNotFoundException {
        Session s = Session.getInstance(new Properties());
        //InputStream is = new ByteArrayInputStream(this.MIME.getBytes());
        String path = getClass().getResource("/MIME.txt")
                .getPath();
        InputStream ms = new FileInputStream(path);
        MimeMessage message = new MimeMessage(s, this.mime_path);

        message.getAllHeaderLines();
        for (Enumeration<Header> e = message.getAllHeaders();
                e.hasMoreElements();) {
            Header h = e.nextElement();
            //System.out.println("DEBUG1: " + h.getName() + " : "
            //        + h.getValue() + "\n");
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
        toString();
    }

    @Override
    public final String toString() {
        String output = "Message-ID=" + field_message_id + ","
                + "Date=" + field_date + ","
                + "From=" + field_from + ","
                + "To=" + field_to + ","
                + "Subject=" + field_subject + ","
                + "MIME-Version=" + field_mime_version + ",";
        return output;
    }
}
