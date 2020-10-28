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
import org.jfree.chart.renderer.xy.XYBarRenderer;
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
 * - threshold_coeficient (default 6.0)
 * - relative_value_0 (default 1)
 * - relative_value_1 (default 3)
 *
 * @author Thibault Debatty
 */
public class Frequency implements DetectionAgentInterface {

    /**
     * Set default time window parameter.
     */
    private static final int DEFAULT_TIME_WINDOW = 604800;
    private static final String TIME_WINDOW_STRING = "time_window";
    private int time_window;

    /**
     * Sampling interval (in second).
     */
    private static final int DEFAULT_SAMPLING_INTERVAL = 60; // in seconds
    private static final String SAMPLING_STRING = "sampling_interval";
    private int sampling_interval;

    /**
     * Min number of raw data needed.
     */
    private static final int DEFAULT_MIN_RAW_DATA = 50;
    private static final String MIN_RAW_DATA_STRING = "min_raw_data";
    private int min_raw_data;

    /**
     * Minimum peak value above average to generate an evidence.
     */
    private static final double DEFAULT_THRESHOLD_COEFICIENT = 6.0;
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

        long start_time = event.getTimestamp() - time_window;
        long end_time = event.getTimestamp();

        RawData[] data = si.findRawData(
                event.getLabel(),
                event.getSubject(),
                start_time,
                end_time);

        if (data.length < min_raw_data) {
            return;
        }

        // count the number of data records in each time bin
        int[] time_bins = bin(data, start_time, end_time);

        // Perform FFT
        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);
        Complex[] transform
                = fft_transformer.transform(
                        intToDoubleArray(time_bins), TransformType.FORWARD);

        // Take lower half
        // Extract frequencies and corresponding power values
        double[] values = new double[transform.length / 2];
        double[] freqs = new double[transform.length / 2];
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

        String figure_spectrum_path = createSpectrumFigure(freqs, values,
                base_peak_freq,
                computeThreshold(values),
                event.getSubject().toString(), data[0].getTime());

        String figure_smooth_spectrum_path = createSpectrumFigure(freqs,
                smoothed_values, base_peak_freq,
                smoothed_threshold,
                "smoothing for " + event.getSubject().toString(),
                data[0].getTime());

        String figure_timeseries_path = createTimeseriesFigure(time_bins,
                start_time,
                event.getSubject().toString());

        String figures = "Spectrum: " + figure_spectrum_path + "\n"
                        + "Smoothed: " + figure_smooth_spectrum_path + "\n"
                        + "Histogram: " + figure_timeseries_path + "\n";
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
                + "| score: " + score + "\n"
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

        this.threshold_coeficient = profile.getParameterDouble(
                THRESHOLD_STRING, DEFAULT_THRESHOLD_COEFICIENT);

        this.min_raw_data = profile.getParameterInt(
                MIN_RAW_DATA_STRING, DEFAULT_MIN_RAW_DATA);

        this.sampling_interval = profile.getParameterInt(
                SAMPLING_STRING, DEFAULT_SAMPLING_INTERVAL);

        this.time_window = profile.getParameterInt(
                TIME_WINDOW_STRING, DEFAULT_TIME_WINDOW);

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
    private String createTimeseriesFigure(
            final int[] time_bins,
            final long start_time,
            final String title) throws IOException {

        XYSeries series = new XYSeries("Time Series");
        for (int i = 0; i < time_bins.length; i++) {
            long bin_timestamp = (long) (start_time + i * sampling_interval);
            series.add(bin_timestamp, time_bins[i]);
        }

        XYSeriesCollection series_collection =
                new XYSeriesCollection(series);

        final JFreeChart chart = ChartFactory.createXYBarChart(
                "Frequency Time Sequence between (Client:Server) " + title,
                "Time",
                true,
                "Number of Requests",
                series_collection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);

        //create the folders to store the figure
        File figure_path = new File("/tmp/mark_figures/");
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

            if (peak_value < threshold) {
                break;
            }

            int peak_index = indexOf(values, peak_value);
            peaks.put(peak_index, peak_value);
            removePeak(values_to_analyze, peak_index);
        }

        if (peaks.values().isEmpty()) {
            return 0;
        }

        int base_peak_index = Collections.min(peaks.keySet());
        double base_peak_value = peaks.get(base_peak_index);

        return base_peak_value;

    }

    private double[] intToDoubleArray(final int[] ints) {
        double[] doubles = new double[ints.length];
        for (int i = 0; i < ints.length; i++) {
            doubles[i] = ints[i];
        }
        return doubles;
    }

    private int[] bin(
            final RawData[] data,
            final long start_time,
            final long end_time) {
        // count the number of elements in each time bin
        long size = pow2gt(time_window / sampling_interval);
        int[] time_bins = new int[(int) size];
        for (RawData record : data) {
            long time = record.getTime();
            if (time < start_time) {
                throw new IllegalArgumentException(
                        "time < start_time : " + time
                        + " : " + start_time);
            }

            if (time > end_time) {
                throw new IllegalArgumentException(
                        "time > end_time : " + time
                        + " : " + end_time);
            }

            long position = (time - start_time) / sampling_interval;
            time_bins[(int) position]++;
        }

        return time_bins;
    }
}
