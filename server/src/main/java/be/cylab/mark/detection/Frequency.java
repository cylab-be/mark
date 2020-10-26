/*
 * The MIT License
 *
 * Copyright 2020 tibo.
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

import be.cylab.mark.core.DetectionAgentInterface;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Perform a frequency analysis over the data regarding this subject.
 *
 * Will trigger an alert when a frequency peak is largely above the average.
 *
 * The threshold for producing an alert is computed as
 * average + threshold_coeficient * standard deviation.
 *
 * The score is computed using the relative_peak_value = peak_value / threshold
 * score = 0 if relative_peak_value is less than relative_value_0
 * score = 1 if relative_peak_value is more than relative_value_1
 * perform linear regression between these values
 *
 * Parameters:
 * - time_window in seconds (default 604800 - 1 week)
 * - sampling_interval in seconds (default 60 seconds)
 * - min_raw_data (default 50)
 * - threshold_coeficient (default 1.3)
 * - relative_value_0 (default 1)
 * - relative_value_1 (default 3)
 *
 * @author Thibault Debatty
 */
public class Frequency implements DetectionAgentInterface {

    /**
     * Set default time window parameter.
     */
    private static final long DEFAULT_TIME_WINDOW = 604800;
    private static final String TIME_WINDOW_STRING = "time_window";
    private long time_window;

    /**
     * Sampling interval (in second).
     */
    private static final long SAMPLING_INTERVAL = 60; // in seconds
    private static final String SAMPLING_STRING = "sampling_interval";
    private long sampling_interval;

    /**
     * Min number of raw data needed.
     */
    private static final int DEFAULT_MIN_RAW_DATA = 50;
    private static final String MIN_RAW_DATA = "min_raw_data";
    private int min_raw_data;

    /**
     * Minimum peak value above average to generate an evidence.
     */
    private static final double DEFAULT_THRESHOLD_COEFICIENT = 1.3;
    private static final String THRESHOLD_STRING = "threshold_coeficient";
    private double threshold_coeficient;

    private static final double DEFAULT_VALUE_0 = 1.0;
    private static final String VALUE_0_STRING = "relative_value_0";

    private static final double DEFAULT_VALUE_1 = 3.0;
    private static final String VALUE_1_STRING = "relative_value_1";

    /**
     * Analyze function inherited from the DetectionAgentInterface.
     *
     * accepts the subject to analyze trigger of the agent the profile used
     * to load the agent the database to which to connect to gather RawData
     * @param event
     * @param dap
     * @param si
     * @throws Throwable
     */
    @Override
    public final void analyze(final Event event,
            final DetectionAgentProfile dap,
            final ServerInterface si) throws Throwable {

        //fetch any needed parameters from the configuration file
        initParams(dap);


        RawData[] data = si.findRawData(
                event.getLabel(),
                event.getSubject(),
                event.getTimestamp() - time_window,
                event.getTimestamp());

        if (data.length < min_raw_data) {
            return;
        }


        long[] times = new long[data.length];
        for (int i = 0; i < data.length; i++) {
            times[i] = data[i].getTime();
        }
        long min_time = min(times);
        long max_time = max(times);
        long size = pow2gt((max_time - min_time) / sampling_interval);

        // count the number of elements in each time bin
        double[] counts = new double[(int) size];
        for (long time : times) {
            long position = (time - min_time) / sampling_interval;
            position = Math.min(position, counts.length - 1);
            counts[(int) position]++;
        }

        // Perform FFT
        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);
        Complex[] transform
                = fft_transformer.transform(counts, TransformType.FORWARD);

        // Take lower half
        double[] values = new double[transform.length / 2];
        double[] freqs = new double[transform.length / 2];

        // Extract frequencies and corresponding power values
        for (int i = 0; i < transform.length / 2; i++) {
            freqs[i] = 1.0 / sampling_interval * i / transform.length;
            values[i] = transform[i].multiply(transform[i].conjugate()).abs();
        }

        // smooth values
        double[] smoothed_values = smooth(values);

        // Remove DC component
        for (int i = 0; i < 10; i++) {
            values[i] = 0;
            smoothed_values[i] = 0;
        }

        //compute the threshold
        double smoothed_threshold = computeThreshold(smoothed_values);

        //
        double base_peak_value = findBaseFrequencyValue(
                smoothed_values, smoothed_threshold);

        if (base_peak_value == 0) {
            return;
        }

        double base_peak_freq = freqs[
                indexOf(smoothed_values, base_peak_value)];
        double relative_peak_value = base_peak_value / smoothed_threshold;

        FuzzyLogic fuzzy = new FuzzyLogic(
                dap.getParameterDouble(VALUE_0_STRING, DEFAULT_VALUE_0),
                dap.getParameterDouble(VALUE_1_STRING, DEFAULT_VALUE_1),
                0.0,
                1.0);
        double score = fuzzy.determineMembership(relative_peak_value);

        if (score == 0) {
            return;
        }

        String figure_spectrum_path = createSpectrumFigure(freqs, values,
                base_peak_freq,
                computeThreshold(values),
                event.getSubject().toString(), data[0].getTime());

        String figure_smooth_spectrum_path = createSpectrumFigure(freqs,
                smoothed_values, base_peak_freq,
                smoothed_threshold,
                "smoothing for " + event.getSubject().toString(),
                data[0].getTime());

        String figure_timeseries_path = createTimeseriesFigure(times,
                event.getSubject().toString(), data[0].getTime());

        String figures = "<a href=\"file:///" + figure_spectrum_path
                        + "\">" + "Frequency Spectrum Figure</a> | "
                        + "<a href=\"file:///" + figure_smooth_spectrum_path
                        + "\">" + "Smoothed Frequency Spectrum Figure</a>"
                        + " | <a href=\"file:///" + figure_timeseries_path
                        + "\">" + "Frequency Time Series Figure</a>";
        //generate report
        /*String freq_report = generateReport(
            "Found frequency peak for " + event.getSubject().toString()
                + "<br />with frequency " + min_freq_value + "\n"
                + "= " + (1 / min_freq_value) + " seconds\n"
                + "with score of: " + msf_result,
            raw_data.length, figures, parameters);*/

        String freq_report =
            "Found frequency peak for " + event.getSubject().toString()
                + " with frequency: " + base_peak_freq + "Hz "
                + "| interval: " + Math.round(1 / base_peak_freq)
                + " seconds"
                + "| score: " + score
                + figures;

        Evidence evidence = new Evidence();
        evidence.setScore(score);
        evidence.setSubject(event.getSubject());
        evidence.setLabel(dap.getLabel());
        evidence.setTime(data[data.length - 1].getTime());
        evidence.setReport(freq_report);
        si.addEvidence(evidence);
    }

    /**
     * Method for loading all possible parameters from the config *.yaml file.
     * It calls to super.initReportTemplate() to also load the report_template
     * to be used when generating the report.
     * @param profile
     */
    final void initParams(final DetectionAgentProfile profile) {

        //check for parameters set through the config file
        threshold_coeficient = DEFAULT_THRESHOLD_COEFICIENT;
        try {
            String threshold_string =
                    profile.getParameter(THRESHOLD_STRING);
            if (threshold_string != null) {
                threshold_coeficient =
                        Double.valueOf(threshold_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency detection threshold"
                    + " from configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        min_raw_data = DEFAULT_MIN_RAW_DATA;
        try {
            String threshold_string = profile.getParameter(MIN_RAW_DATA);
            if (threshold_string != null) {
                min_raw_data =
                        Integer.valueOf(threshold_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get fminimum raw data threshold"
                    + " from configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        sampling_interval = SAMPLING_INTERVAL;
        try {
            String sampling_string = profile.getParameter(SAMPLING_STRING);
            if (sampling_string != null) {
                sampling_interval = Integer.valueOf(sampling_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency sampling interval from"
                    + " configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        time_window = DEFAULT_TIME_WINDOW;
        try {
            String time_window_string = profile.getParameter(
                    TIME_WINDOW_STRING);
            if (time_window_string != null) {
                time_window = Long.valueOf(time_window_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency time window parameters"
                    + " from configuration file. Error: " + ex.getMessage());
        }
    }

    /**
     * Function searches for a number bigger than value and is a power of 2.
     * @param value
     * @return long value
     */
    static long pow2gt(final long value) {
        long result = 1;

        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    static long min(final long[] values) {
        long result = Long.MAX_VALUE;
        for (long value : values) {
            if (value < result) {
                result = value;
            }
        }
        return result;
    }

    static long max(final long[] values) {
        long result = Long.MIN_VALUE;
        for (long value : values) {
            if (value > result) {
                result = value;
            }
        }
        return result;
    }

    static double max(final double[] values) {
        double result = Double.MIN_VALUE;
        for (double value : values) {
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

    private int indexOf(final double[] values, final double value) {
        int index_of_value = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                index_of_value = i;
                break;
            }
        }
        return index_of_value;
    }

    /**
     * Compute a threshold value using 3-sigma rule.
     *
     * https://en.wikipedia.org/wiki/68%E2%80%9395%E2%80%9399.7_rule
     * threshold = average + frequency_threshold * std deviation
     *
     * @param values
     * @return
     */
    private double computeThreshold(final double[] values) {

        double average = average(values);

        // compute standard deviation sigma
        //sum of (value - average)^2
        double sigma = 0;
        for (double value: values) {
            sigma += Math.pow(value - average, 2);
        }
        sigma = sigma / (double) (values.length - 1);
        sigma = Math.sqrt(sigma);

        //threshold is equal to the average + detection_parameter * the sigma
        return average + threshold_coeficient * sigma;
    }

    /**
     * Method for drawing and saving to a file the timeseries figure.
     * @param times
     * @param title
     * @param time
     * @return
     * @throws IOException
     */
    private String createTimeseriesFigure(final long[] times,
                            final String title,
                            final long time) throws IOException {
        XYSeries series = new XYSeries("Time Series");
        //sort the time array so the timestamps are in order
        Arrays.sort(times);
        //create the arrays with bins
        int nr_of_bins = 512;
        double period_in_seconds = (double) (times[times.length - 1]
                                - times[0]) / nr_of_bins;
        int[] timeseries_bins = new int[nr_of_bins];

        //fill the bins
        for (long timestamp : times) {
            long req_time = timestamp - times[0];
            long global_time = times[times.length - 1] - times[0];
            int i_bin = Math.round((req_time / (float) global_time) * 512);
            if (i_bin == 512) {
                continue;
            }
            if (timeseries_bins[i_bin] == 0) {
                timeseries_bins[i_bin] = 1;
            }
        }

        //fill the time series with the data from the bins
        for (int i = 0; i < timeseries_bins.length; i++) {
            long bin_timestamp = (long) (times[0] + i * period_in_seconds);
            series.add(bin_timestamp, timeseries_bins[i]);
        }

        XYSeriesCollection series_collection =
                new XYSeriesCollection(series);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Frequency Time Sequence between (Client:Server) " + title,
                "Time",
                "Number of Requests",
                series_collection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getXYPlot();
        XYLineAndShapeRenderer renderer =
                (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesLinesVisible(
                series_collection.getSeriesCount(), false);

        //transform timestamp to create day folder for the figures
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String formated_date = sf.format(date);
        //create the folders to store the figure
        File figure_path = new File("/tmp/mark_figures/"
                                    + formated_date
                                    + "/");
        figure_path.mkdirs();
        //create the temporary figure file
        File figure = File.createTempFile("frequency_time_chart", ".png",
                        figure_path);
        //load the chart into the figure file
        ChartUtilities.saveChartAsPNG(figure, chart, 1600, 1200);
        return figure.getAbsolutePath();
    }

    /**
     * Method for generating a figure from the data recovered from the database.
     * Method for generating a graph from the data recovered from the database.
     * @param dataset
     * @param title
     * @throws IOException
     */
    private String createSpectrumFigure(final double[] freqs,
                            final double[] values,
                            final double lowest_frequency,
                            final double threshold,
                            final String title,
                            final long time)
            throws IOException {

        final XYSeries series_average = new XYSeries("Threshold");
        series_average.add(freqs[0], threshold);
        series_average.add(freqs[freqs.length - 1], threshold);
        final XYSeries series = new XYSeries("Spectrum");
        for (int n = 0; n < freqs.length; n++) {
            series.add(freqs[n], values[n]);
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        data.addSeries(series_average);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Frequency Spectrum between (Client:Server) " + title,
                "Frequency (Hz)",
                "Y",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        //set the numerical ranges for the Domain(X) and Range(Y)
        XYPlot xyplot = chart.getXYPlot();
        xyplot.setBackgroundAlpha(0);
        NumberAxis domain = (NumberAxis) xyplot.getDomainAxis();
        domain.setRange(0, lowest_frequency * 20);

        //transform timestamp to create day folder for the figures
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String formated_date = sf.format(date);
        //create the folders to store the figure
        File figure_path = new File("/tmp/mark_figures/"
                                    + formated_date
                                    + "/");
        figure_path.mkdirs();
        //create the temporary figure file
        File figure = File.createTempFile("frequency_spectrum_chart", ".png",
                        figure_path);
        //load the chart into the figure file
        ChartUtilities.saveChartAsPNG(figure, chart, 1600, 1200);
        return figure.getAbsolutePath();
    }

    private double[] smooth(final double[] values) {
        double[] new_values = new double[values.length];
        int[] smoothing_window = new int[]{1, 2, 4, 2, 1};
        for (int i = 0; i < 10; i++) {
            for (int n = 2; n < values.length - 3; n++) {
                new_values[n] = (values[n - 2] * smoothing_window[0]
                            + values[n - 1] * smoothing_window[1]
                            + values[n] * smoothing_window[2]
                            + values[n + 1] * smoothing_window[3]
                            + values[n + 2] * smoothing_window[4]) / 10;
            }
        }
        return new_values;
    }

    /**
     * Remove a frequency peak by overwritting 5 points.
     * - the point with value peak_value
     * - the 2 points situated before the acutal peak
     * - the 2 points situated right after the peak
     *
     * @param values
     * @param peak_value
     */
    private void removePeak(final double[] values, final int index_of_peak) {
        double[] erasure_values = new double[]{0.2, 0.1, 0, 0.1, 0.2};

        values[index_of_peak - 2] = values[index_of_peak - 2]
                                    * erasure_values[0];
        values[index_of_peak - 1] = values[index_of_peak - 1]
                                    * erasure_values[1];
        values[index_of_peak] = values[index_of_peak] * erasure_values[2];
        if (index_of_peak < values.length - 1) {
            values[index_of_peak + 1] = values[index_of_peak + 1]
                                        * erasure_values[3];
        }
        if (index_of_peak < values.length - 2) {
            values[index_of_peak + 2] = values[index_of_peak + 2]
                                        * erasure_values[4];
        }
    }

    /**
     * We will probably have multiple peaks above the threshold, so we search
     * and return the peak with the lowest frequency.
     *
     * @param values
     * @param threshold
     * @return the value of the peak
     */
    private double findBaseFrequencyValue(final double[] values,
                                    final double threshold) {

        double[] values_to_analyze = values.clone();
        Map<Integer, Double> peaks = new HashMap<>();
        while (true) {
            double peak_value = max(values_to_analyze);
            if (peak_value > threshold) {
                int peak_index = indexOf(values, peak_value);
                peaks.put(peak_index, peak_value);
                removePeak(values_to_analyze, peak_index);
            }

            if (peak_value < threshold) {
                break;
            }
        }

        if (peaks.values().isEmpty()) {
            return 0;
        }


        int base_peak_index = Collections.min(peaks.keySet());
        double base_peak_value = peaks.get(base_peak_index);

        return base_peak_value;

    }


}
