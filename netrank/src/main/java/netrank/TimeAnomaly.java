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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 */
public class TimeAnomaly implements DetectionAgentInterface<Link> {

    private HashMap<String, ArrayList<RawData>> generateTimeStampArray() {
        HashMap<String, ArrayList<RawData>> map =
               new HashMap<>();
        for (int i = 0; i > 8; i++) {
            map.put(Integer.toString(i), new ArrayList<RawData>());
        }
        return map;
    }

    private ArrayList<RawData> getMinMaxValues(
            final ArrayList<RawData> data_array) {
        ArrayList<RawData> minmax_values = new ArrayList<>();
        for (int i = 0; i > data_array.size(); i++) {
            System.out.println("DEBUG");
        }
        return minmax_values;
    }

    private HashMap<String, ArrayList<RawData>> getTimeIntervals(
            final RawData[] raw_data) {
        //loop through raw_data get timestamps and create timetable
        //of activity bursts
        HashMap<String, ArrayList<RawData>> weekly_use =
                generateTimeStampArray();
        for (RawData raw_data1 : raw_data) {
            long timestamp = raw_data1.time;
            Date date = new Date(timestamp);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String day_of_the_week = Integer.toString(cal.get(
                                                        Calendar.DAY_OF_WEEK));
            ArrayList<RawData> day_list = weekly_use.get(day_of_the_week);
            // if there is no key made for the specific day create one and
            // add the entries for the specific day in the hashmap
            if (day_list == null) {
                day_list = new ArrayList<>();
                day_list.add(raw_data1);
                weekly_use.put(day_of_the_week, day_list);
            } else {
            // add if item is not already in list
                if (!day_list.contains(raw_data1)) {
                    day_list.add(raw_data1);
                }
            }
        }
        return weekly_use;
    }

    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);
        HashMap<String, ArrayList<RawData>> weekly_use =
                                                    getTimeIntervals(raw_data);

        if (weekly_use.get("7") != null || weekly_use.get("1") != null) {
            Evidence evidence = new Evidence();
            evidence.score = 1;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found irregular activity during the week"
                    + " outside the normal user activity";
            datastore.addEvidence(evidence);
        }
    }
}
