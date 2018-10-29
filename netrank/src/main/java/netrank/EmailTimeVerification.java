/*
 * The MIT License
 *
 * Copyright 2018 georgi.
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 */
public class EmailTimeVerification implements DetectionAgentInterface<Link> {
    private static final double RATIO_THRESHOLD = 0.3;
    private static final int TIME_HOUR_LOWER_THRESHOLD = 8;
    private static final int TIME_HOUR_UPPER_THRESHOLD = 19;

    private ArrayList<String> getSendingTime(final RawData[] raw_data)
            throws IOException {
        ArrayList sending_times = new ArrayList();
        for (RawData raw_data1 : raw_data) {
            String data = raw_data1.data;
            MIMEParser parser = new MIMEParser(data);
            sending_times.add(parser.getDate());
        }
        return sending_times;
    }

    private int getAbnormalTimeCount(final ArrayList<String> sending_times)
            throws ParseException {
        int time_count = 0;
        for (int i = 0; i < sending_times.size(); i++) {
            String sending_time = sending_times.get(i);
            DateFormat format =
                    new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
                            Locale.ENGLISH);
            Date date = format.parse(sending_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hour_of_day = cal.get(Calendar.HOUR_OF_DAY);
            boolean is_working_day = ((hour_of_day >= TIME_HOUR_LOWER_THRESHOLD)
                    && (hour_of_day <= TIME_HOUR_UPPER_THRESHOLD));
            int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
            boolean is_weekday = ((day_of_week >= Calendar.MONDAY)
                    && (day_of_week <= Calendar.FRIDAY));

            if (!is_working_day || !is_weekday) {
                time_count = time_count + 1;
            }
        }
        return time_count;
    }

    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface<Link> datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        if (raw_data.length < 50) {
            return;
        }

        ArrayList sending_dates = getSendingTime(raw_data);
        int abnormal_time_count = getAbnormalTimeCount(sending_dates);
        double email_abnormal_time_ratio =
                    abnormal_time_count / (double) raw_data.length;
        if (email_abnormal_time_ratio > RATIO_THRESHOLD) {
            Evidence evidence = new Evidence();
            evidence.score = email_abnormal_time_ratio;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found emails with suspicious amount"
                    + " of abnormal times"
                    + " between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious ratio of : "
                    + email_abnormal_time_ratio + " between the amount of"
                    + " abnormal times and the total amount of emails \n";
            datastore.addEvidence(evidence);
            }
    }
}
