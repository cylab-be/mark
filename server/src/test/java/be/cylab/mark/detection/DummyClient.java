/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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
package be.cylab.mark.detection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import be.cylab.mark.core.Subject;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * A fake connection to MARK server, can be used for testing and debugging. It
 * will simply return fake data, and write out what it receives...
 *
 * @author Thibault Debatty
 */
public class DummyClient<T extends Subject> implements ServerInterface<T> {

    public String test() throws Throwable {
        return "test";
    }

    public void testString(String data) throws Throwable {

    }

    public void addRawData(RawData data) throws Throwable {
        System.out.println(data);
    }

    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
    }

    @Override
    public ObjectId addFile(byte[] bytes, String filename) throws Throwable {
        System.out.println(bytes);
        return null;
    }

    private static final int N_APT = 1000;
    private static final int N_NOISE = 10000;
    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;

    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        int start = 123456;
        Random rand = new Random();

        RawData[] data = new RawData[N_APT + N_NOISE];

        for (int i = 0; i < N_APT; i++) {
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(start + APT_INTERVAL * i);
            data[i].setData(data[i].getTime() + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "400"
                    + " 918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + "175.193.216.231 text/html");
        }

        // Add a few random requests
        for (int i = N_APT; i < N_APT + N_NOISE; i++) {
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(start + rand.nextInt(N_APT * APT_INTERVAL));
            data[i].setData(data[i].getTime() + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "200"
                    + " 918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + "175.193.216.231 text/html");
        }

        return data;
    }

    public Evidence<T>[] findEvidence(String label, T subject) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Evidence<T>[] findEvidence(String label) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Evidence<T> findEvidenceById(String id) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getURL() {
        try {
            return new URL("http://dummy.to.u:155");
        } catch (MalformedURLException ex) {

        }

        return null;
    }

    public Evidence<T>[] findLastEvidences(String label, T subject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RawData[] findData(Document query) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] findFile(ObjectId file_id) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getFromCache(String key) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void storeInCache(String key, Object value) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean compareAndSwapInCache(
            String key, Object new_value, Object old_value) throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> status() throws Throwable {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
