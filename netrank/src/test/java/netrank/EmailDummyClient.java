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

import java.util.LinkedList;
import java.util.Random;
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
    String email_Mime = "MIME-Version: 1.0\n" +
"Content-Type: multipart/alternative; boundary=\"outer-boundary\"\n" +
"\n" +
"This is a MIME-encoded message. If you are seeing this, your mail\n" +
"reader is old.\n" +
"--outer-boundary\n" +
"Content-Type: text/plain; charset=us-ascii\n" +
"\n" +
"This message might make you :) or it might make you :(\n" +
"\n" +
"--outer-boundary\n" +
"MIME-Version: 1.0\n" +
"Content-Type: multipart/related;\n" +
"  type=\"text/html\"; start=\"<body@here>\"; boundary=\"inner-boundary\"\n" +
"\n" +
"--inner-boundary\n" +
"Content-Type: text/html; charset=us-ascii\n" +
"Content-Disposition: inline\n" +
"Content-ID: <body@here>\n" +
"\n" +
"<html>\n" +
" <body>\n" +
"  This message might make you\n" +
"  <img src=\"cid:smile@here\" alt=\"smile\">\n" +
"  or it might make you\n" +
"  <img src=\"cid:frown@here\" alt=\"frown\">\n" +
" </body>\n" +
"</html>\n" +
"\n" +
"--inner-boundary\n" +
"Content-Type: image/gif\n" +
"Content-Disposition: inline\n" +
"Content-Transfer-Encoding: base64\n" +
"Content-ID: <smile@here>\n" +
"\n" +
"R0lGODlhEAAQAKEBAAAAAP//AP//AP//ACH5BAEKAAIALAAAAAAQABAAAAIzlA2px6IBw2\n" +
"IpWglOvTahDgGdI0ZlGW5meKlci6JrasrqkypxJr8S0oNpgqkGLtcY6hoFADs=\n" +
"\n" +
"--inner-boundary\n" +
"Content-Type: image/gif\n" +
"Content-Disposition: inline\n" +
"Content-Transfer-Encoding: base64\n" +
"Content-ID: <frown@here>\n" +
"\n" +
"R0lGODlhEAAQAKEBAAAAAAD//wD//wD//yH5BAEKAAIALAAAAAAQABAAAAIzlA2px6IBw2\n" +
"IpWglOvTahDgGdI0ZlGW5meKlci75drDzm5uLZyZ1I3Mv8ZB5Krtgg1RoFADs=\n" +
"\n" +
"--inner-boundary--\n" +
"\n" +
"--outer-boundary--";

    private RawData[] generateData(String type, Link subject) {
        RawData[] data = new RawData[N];
        int start = 123456;
        Random rand = new Random();

        // Add a few random requests
        for (int i = N; i < N + N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(5 * APT_INTERVAL);
            data[i].data = email_Mime;
        }
        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] result = generateData(type, (Link) subject);
        //System.out.println("DEBUG: " + result.length);
        return result;
    }

    @Override
    public RawData[] findData(Document query)
            throws Throwable {

        RawData[] data = generateData("data.http",
                 new Link("192.168.2.3", " "));
        //System.out.println("DEBUG: " + result.length);
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
