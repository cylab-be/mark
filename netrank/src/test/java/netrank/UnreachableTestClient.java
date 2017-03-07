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


public class UnreachableTestClient<T extends Subject> extends DummyClient<T> {

    private static final int N = 10000;

    private final double ratio;
    private final LinkedList<Evidence> evidences = new LinkedList<>();

    public UnreachableTestClient(final double ratio) {
        this.ratio = ratio;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        int start = 123456;
        Random rand = new Random();

        RawData[] data = new RawData[N];

        for (int i = 0; i < N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + i;

            // TCP_HIT = found in cache
            // http://wiki.squid-cache.org/SquidFaq/SquidLogs
            String status = "TCP_HIT/200";

            if (rand.nextDouble() < ratio) {
                // 504 = gateway timeout (aka server unreachable)
                status = "TCP_MISS/504";
            }

            if (rand.nextDouble() < 0.01) {
                // Something corrupted
                status = "TCP_HIT/123";
            }

            data[i].data = data[i].time + "    "
                + "126 "
                + "198.36.158.8 "
                + status
                + "918 GET "
                + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                + "175.193.216.231 text/html";
        }
        if (ratio != 0) {
            return data;
        } else {
            return new RawData[0];
        }
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
