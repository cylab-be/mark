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

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import mark.activation.DummyClient;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;

/**
 *
 * @author georgi
 * @param <T>
 */
public class TimeAnomalyTestClient <T extends Subject> extends DummyClient<T> {

    private static final int N = 5000;
    private static final int M = 500;

    private final LinkedList<Evidence> evidences = new LinkedList<>();

    private RawData[] GenerateAbnormalData(String type, T subject) {
        RawData[] data = new RawData[M];
        return data;
    }

    private RawData[] GenerateNormalData(String type, T subject) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        // date.getTime() Returns the number of milliseconds since 
        // January 1, 1970, 00:00:00 GMT represented by this Date object.
        long start = date.getTime();
        System.out.println("START: " + start + " " + date.toString());
        Random rand = new Random();

        RawData[] data = new RawData[N];

        for (int i = 0; i < N; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + (i * 86400);
            data[i].data = data[i].time + "    "
                + "126 "
                + "198.36.158.8 "
                + "TCP_HIT/200"
                + " 918 GET "
                + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                + "175.193.216.231 text/html";
        }
        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] raw_data = GenerateNormalData(type, subject);
        return raw_data;
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
