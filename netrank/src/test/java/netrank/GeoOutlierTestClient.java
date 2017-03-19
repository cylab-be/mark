/*
 * The MIT License
 *
 * Copyright 2017 Georgi Nikolov.
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

/**
 *
 * @author Georgi Nikolov
 * @param <T>
 */
public class GeoOutlierTestClient<T extends Subject> extends DummyClient<T> {

        private static final int N = 10000;

    private final int N_APT;
    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;
    private static final String APT_SERVER = "105.244.103.0";
    private static final String SERVER = "175.193.216.231";
    private final LinkedList<Evidence> evidences = new LinkedList<>();

    public GeoOutlierTestClient(final int nmbr_outliers) {
        this.N_APT = nmbr_outliers;
    }

    @Override
        public RawData[] findRawData(String type, T subject)
            throws Throwable {

        int start = 123456;
        Random rand = new Random();

        RawData[] data = new RawData[N_APT + N];

        for (int i = 0; i < N_APT; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + APT_INTERVAL * i;
            data[i].data = data[i].time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "400"
                    + "918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + APT_SERVER
                    + " text/html";
        }

        // Add a few random requests
        for (int i = N_APT; i < N_APT + N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(N_APT * APT_INTERVAL);
            data[i].data = data[i].time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "200"
                    + "918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + SERVER
                    + " text/html";
        }

        return data;
    }

    @Override
    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
        evidences.add(evidence);
    }

    public LinkedList<Evidence> getEvidences() throws Throwable {
        return evidences;
    }
}