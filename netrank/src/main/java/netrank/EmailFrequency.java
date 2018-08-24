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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import static netrank.Frequency.min;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author georgi
 */
public class EmailFrequency implements DetectionAgentInterface<Link> {

        /**
     * Sampling interval (in second).
     */
    public static final long SAMPLING_INTERVAL = 2; // in seconds

    /**
     * Minimum peak to generate an evidence.
     */
    public static final double DETECTION_THRESHOLD = 10.0;

    static long pow2gt(final long value) {
        long result = 1;

        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    static long min(final long[] values) {
        long result = Integer.MAX_VALUE;
        for (long value : values) {
            if (value < result) {
                result = value;
            }
        }
        return result;
    }

    static long max(final long[] values) {
        long result = Integer.MIN_VALUE;
        for (long value : values) {
            if (value > result) {
                result = value;
            }
        }
        return result;
    }

    private double average(final double[] values) {
        double result = 0;
        for (double value : values) {
            result += value;
        }

        return result / values.length;
    }

/**
 *
 * @param raw_data
 * @return Map<String, ArrayList> dictionary of linked sender and all timestamps
 * for the corresponding e-mails sent at that time by the sender
 * @throws IOException
 */
    private Map<String, ArrayList> parseMessage(final RawData[] raw_data)
            throws IOException {
        Map<String, ArrayList> dict_sender_date = new HashMap<>();
        for (RawData raw_data1 : raw_data) {
            String email_data = raw_data1.data;
            MIMEParser parser = new MIMEParser(email_data);
            String from_field = parser.getFrom();
            long timestamp = raw_data1.time;
            // add to the map dictionary the person sending the email with the
            //timestamp of the email if its the first entry in the dictionary
            if (!dict_sender_date.containsKey(from_field)) {
                dict_sender_date.put(from_field, new ArrayList());
                dict_sender_date.get(from_field).add(timestamp);
            // else add the timestamp to the corresponding sender
            } else {
                dict_sender_date.get(from_field).add(timestamp);
            }
        }
        return dict_sender_date;
    }

/**
 * Analyze function inherited from the DetectionAgentInterface.
 * accepts the subject to analyze
 * trigger of the agent
 * the profile used to load the agent
 * the database to which to connect to gather RawData
 * @param subject
 * @param actual_trigger_label
 * @param profile
 * @param datastore
 * @throws Throwable
 */
    @Override
    public final void analyze(final Link subject,
                    final String actual_trigger_label,
                    final DetectionAgentProfile profile,
                    final ServerInterface<Link> datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 50) {
            return;
        }
        Map<String, ArrayList> dict_sender_time = parseMessage(raw_data);

        for (String key : dict_sender_time.keySet()) {

            String sender = key;
            ArrayList timestamps = dict_sender_time.get(key);

            FastFourierTransformer fft_transformer = new FastFourierTransformer(
                    DftNormalization.STANDARD);


            long[] times = new long[dict_sender_time.get(key).size()];
            for (int i = 0; i < timestamps.size(); i++) {
                times[i] = (long) timestamps.get(i);
            }

            long min = min(times);
            long max = max(times);
            long size = pow2gt((max - min) / SAMPLING_INTERVAL);

            double[] counts = new double[(int) size];
            for (long time : times) {
                long position = (time - min) / SAMPLING_INTERVAL;
                position = Math.min(position, counts.length - 1);
                counts[(int) position]++;
            }

            Complex[] transform =
                    fft_transformer.transform(counts, TransformType.FORWARD);

            // Take lower half
            double[] values = new double[transform.length / 2];
            double[] freqs = new double[transform.length / 2];

            for (int i = 0; i < transform.length / 2; i++) {
                freqs[i] = 1.0 / SAMPLING_INTERVAL * i / transform.length;
                //values[i] = transform[i].abs();
                // power spectrum
                values[i] =
                        transform[i].multiply(transform[i].conjugate()).abs();
                //        / (raw_data.length * transform.length);
                //System.out.println(values[i]);
            }

            if (values.length < 10) {
                return;
            }

            // Remove DC component
            for (int i = 0; i < 10; i++) {
                values[i] = 0;
            }

            double average = average(values);

            // search for a peak
            for (int i = 0; i < values.length; i++) {
                if (values[i] < DETECTION_THRESHOLD * average) {
                    continue;
                }

                Evidence evidence = new Evidence();
                evidence.score = 0.9;
                evidence.subject = subject;
                evidence.label = profile.label;
                evidence.time = raw_data[raw_data.length - 1].time;
                evidence.report = "Found peak for frequency " + freqs[i] + "\n"
                        + "= " + (1 / freqs[i]) + " seconds\n"
                        + "in the e-mails sent from " + sender + "\n";

                datastore.addEvidence(evidence);
            }
        }
    }
}
