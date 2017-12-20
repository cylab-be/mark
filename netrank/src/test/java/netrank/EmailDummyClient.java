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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import javax.mail.MessagingException;
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

    private final int N;
    private final int S;
    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;
    private LinkedList<Evidence> evidence = new LinkedList<>();
    String email_Mime = "";

    public EmailDummyClient(int noise, int spam) {
        this.N = noise;
        this.S = spam;
    }

    private String parseMIME(String email_path)
            throws FileNotFoundException, MessagingException, IOException {

        String path = getClass().getResource(email_path)
                .getPath();
        FileReader file = new FileReader(path);
        BufferedReader reader = new BufferedReader(file);
        String line = reader.readLine();
        StringBuffer sb = new StringBuffer();
        while(line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }
        return sb.toString();        
    }

    private RawData[] generateData(String type, Link subject)
            throws FileNotFoundException, MessagingException, IOException {

        RawData[] data = new RawData[N + S];
        int start = 123456;
        Random rand = new Random();

        // Normal Traffic
        for (int i = 0; i < N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(5 * APT_INTERVAL);
            data[i].data = parseMIME("/MIME.txt");
        }

        // SPAM Traffic
        for (int i = N; i < N + S; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(5 * APT_INTERVAL);
            data[i].data = parseMIME("/SPAM.txt");
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
