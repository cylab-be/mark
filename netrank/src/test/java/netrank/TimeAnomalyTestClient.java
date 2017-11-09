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
//import java.util.Random;
import mark.activation.DummyClient;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author georgi
 * @param <T>
 */
public class TimeAnomalyTestClient <T extends Subject> extends DummyClient<T> {

    private static final int N = 500;
    private static final int M = 5;

    private final LinkedList<Evidence> evidences = new LinkedList<>();

    private RawData[] GenerateAbnormalData(String type, T subject) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        // date.getTime() Returns the number of milliseconds since 
        // January 1, 1970, 00:00:00 GMT represented by this Date object.
        RawData[] data = new RawData[M];

        for (int i = 0; i < M; i++) {
            calendar.add(Calendar.DATE, 1);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            // if the current day is not Saturday or Sunday add days until it is
            if (day != 7 && day != 1){
                int diff = 7 - day;
                calendar.add(Calendar.DATE, diff);
            }
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = calendar.getTimeInMillis();
            data[i].data = data[i].time + "    "
                + "126 "
                + "198.36.158.8 "
                + "TCP_HIT/200"
                + " 918 GET "
                + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                + "105.244.103.5 text/html";
        }
        return data;
    }

    private RawData[] GenerateNormalData(String type, T subject) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        // date.getTime() Returns the number of milliseconds since 
        // January 1, 1970, 00:00:00 GMT represented by this Date object.
        //Random rand = new Random();

        RawData[] data = new RawData[N];

        for (int i = 0; i < N; i++) {
            //calendar.add(Calendar.HOUR, 1);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == 7){
                calendar.add(Calendar.DATE, 2);
            } else if (day == 1) {
                calendar.add(Calendar.DATE, 1);
            }
            // check if the current time is before 8.30
            boolean low_limit = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE) < 8 * 60 + 30;
            // check if the current time is after 15.30
            boolean high_limit = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE) > 15 * 60 + 30;
            if (low_limit) {
                calendar.set(Calendar.HOUR_OF_DAY, 9);
            }
            if (high_limit) {
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.add(Calendar.DATE, 1);
            }
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = calendar.getTimeInMillis();
            data[i].data = data[i].time + "    "
                + "126 "
                + "198.36.158.8 "
                + "TCP_HIT/200"
                + " 918 GET "
                + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                + "175.193.216.231 text/html";

            calendar.add(Calendar.HOUR, 1);
        }
        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] raw_data_normal = GenerateNormalData(type, subject);
        RawData[] raw_data_abnormal = GenerateAbnormalData(type, subject);
        RawData[] raw_data = ArrayUtils.addAll(raw_data_normal, raw_data_abnormal);

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
