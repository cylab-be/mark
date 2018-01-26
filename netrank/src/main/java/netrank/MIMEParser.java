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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Class responsible for parsing an e-mail in MIME format to an object which
 * fields can be easily accessed via get/set.
 * @author georgi
 */
public class MIMEParser {

    private final String mime_path;
    private String field_message_id;
    private String field_date;
    private String field_from;
    private String field_to;
    private String field_subject;
    private String field_mime_version;
    private String field_content_type_text;
    private String field_content_type_html;
    private ArrayList field_attachment;
    private final String[] attachment_types = {"application/msword",
                                            "application/pdf",
                                            "application/rtf",
                                            "application/zip",
                                            "audio/x-wav",
                                            "image/gif",
                                            "image/jpeg",
                                            "image/jpg",
                                            "image/png"};

    /**
     *
     * @param email_path
     * @throws java.io.IOException exception
     */
    public MIMEParser(final String email_path) throws IOException {
        this.mime_path = email_path;
        this.field_message_id = "";
        this.field_date = "";
        this.field_from = "";
        this.field_to = "";
        this.field_subject = "";
        this.field_mime_version = "";
        this.field_content_type_text = "";
        this.field_content_type_html = "";
        this.field_attachment = new ArrayList();
        try {
            parseMimeEmail();
        } catch (MessagingException | FileNotFoundException ex) {
            System.out.println("Error Parsing MIME Email. Error: " + ex);
        }
    }

    private void parseMimeEmail() throws MessagingException,
            FileNotFoundException,
            IOException {
        Session s = Session.getInstance(new Properties());
        //InputStream is = new ByteArrayInputStream(this.MIME.getBytes());
//        String path = getClass().getResource("/MIME.txt")
//                .getPath();
//        InputStream ms = new FileInputStream(path);
//        MimeMessage message = new MimeMessage(s, this.mime_path);
        MimeMessage message = new MimeMessage(s,
                        new ByteArrayInputStream(this.mime_path.getBytes()));

        message.getAllHeaderLines();
        for (Enumeration<Header> e = message.getAllHeaders();
                e.hasMoreElements();) {
            Header h = e.nextElement();
//            System.out.println("DEBUG1: " + h.getName() + " : "
//                    + h.getValue() + "\n");
            switch (h.getName()) {
                case "Message-ID":
                    this.field_message_id = h.getValue();
                    break;
                case "From":
                    this.field_from = h.getValue();
                    break;
                case "To":
                    this.field_to = h.getValue();
                    break;
                case "Subject":
                    this.field_subject = h.getValue();
                    break;
                case "MIME-Version":
                    this.field_mime_version = h.getValue();
                    break;
                case "Date":
                    this.field_date = h.getValue();
                    break;
                default:
                    break;
            }
        }

        Multipart mp = (Multipart) message.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart bp = mp.getBodyPart(i);
            if (bp.isMimeType("text/plain")) {
                this.field_content_type_text = (String) bp.getContent();
            } else if (bp.isMimeType("multipart/*")) {
                Multipart submp = (Multipart) bp.getContent();
                for (int n = 0; n < submp.getCount(); n++) {
                    BodyPart subbp = submp.getBodyPart(n);
                    if (subbp.isMimeType("text/html")) {
                        this.field_content_type_html =
                                        (String) subbp.getContent();
                    }
                    for (String attachment1 : attachment_types) {
                        if (subbp.isMimeType(attachment1)) {
                            this.field_attachment.add(
                                        subbp.getContent());
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return String field_message_id
     */
    public final String getMessageID() {
        return this.field_message_id;
    }

    /**
     *
     * @return String field_date
     */
    public final String getDate() {
        return this.field_date;
    }

    /**
     *
     * @return String field_from
     */
    public final String getFrom() {
        return this.field_from;
    }

    /**
     *
     * @return String field_to
     */
    public final String getTo() {
        return this.field_to;
    }

    /**
     *
     * @return String field_subject
     */
    public final String getSubject() {
        return this.field_subject;
    }

    /**
     *
     * @return String field_mime_version
     */
    public final String getMIMEVersion() {
        return this.field_mime_version;
    }

    /**
     *
     * @return String field_content_type_text
     */
    public final String getText() {
        return this.field_content_type_text;
    }

    /**
     *
     * @return String field_content_type_html
     */
    public final String getHTML() {
        return this.field_content_type_html;
    }

    /**
     *
     * @return String field_attachment
     */
    public final ArrayList getAttachment() {
        return this.field_attachment;
    }

    @Override
    public final String toString() {
        String output = "Message-ID=" + this.field_message_id + " , "
                + "Date=" + this.field_date + " , "
                + "From=" + this.field_from + " , "
                + "To=" + this.field_to + " , "
                + "Subject=" + this.field_subject + " , "
                + "MIME-Version=" + this.field_mime_version + " , "
                + "Text=" + this.field_content_type_text + " , "
                + "HTML=" + this.field_content_type_html + " , "
                + "Attachments=" + this.field_attachment + " , ";
        return output;
    }
}
