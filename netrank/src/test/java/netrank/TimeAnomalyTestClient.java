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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    private static final int N = 1000;
    private static final int M = 5;

    private final LinkedList<Evidence> evidences = new LinkedList<>();


    private RawData[] filterAndCombineData(RawData[] data) {
        List<RawData> list_data = new ArrayList<>(Arrays.asList(data));
        Iterator<RawData> iterator = list_data.iterator();
        int amount_of_weekend_data = 0;
        int amount_of_outsidehours_data = 0;
        while(iterator.hasNext()) {
            RawData current_raw_data = iterator.next();
            Date date = new Date(current_raw_data.time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            // boolean check if the current time is before 8.30
            boolean low_limit = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE) < 8 * 60 + 30;
            // boolean check if the current time is after 15.30
            boolean high_limit = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE) > 15 * 60 + 30;

            if (day == 7 || day == 1) {
                if (amount_of_weekend_data > M) {
                    iterator.remove();
                } else {
                    amount_of_weekend_data = 1 + amount_of_weekend_data;
                }
            } else if (low_limit || high_limit) {
                if (amount_of_outsidehours_data > M) {
                    iterator.remove();
                } else {
                    amount_of_outsidehours_data = 1 +
                            amount_of_outsidehours_data;
                }
            }
        }
        RawData[] filtered_data = list_data.toArray(
                new RawData[list_data.size()]);
        return filtered_data;
    }

    private RawData[] generateData(String type, T subject) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        // date.getTime() Returns the number of milliseconds since 
        // January 1, 1970, 00:00:00 GMT represented by this Date object.
        RawData[] data = new RawData[N];
        //create one loop of simulated connections, filter out the suspicious
        //connections and add couple of them afterwards
        for (int i = 0; i < N; i++) {
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

            calendar.add(Calendar.HOUR, 2);
        }
        data = filterAndCombineData(data);
        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] raw_data = generateData(type, subject);

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
