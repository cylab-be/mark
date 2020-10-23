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
import java.text.DateFormat;
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
 *
 * @author Thibault Debatty
 */
public class Frequency implements DetectionAgentInterface {

    /**
     * Set default time window parameter.
     */
    private static final long DEFAULT_TIME_WINDOW = 86400;
    private static final String TIME_WINDOW_STRING = "time_window";
    private static long time_window_param;

    /**
     * Sampling interval (in second).
     */
    private static final long SAMPLING_INTERVAL = 2; // in seconds
    private static final String SAMPLING_STRING = "sampling_interval";
    private static long sampling_detection_interval;

    /**
     * Min number of raw data needed.
     */
    private static final int DEFAULT_MIN_RAW_DATA = 50;
    private static final String MIN_RAW_DATA = "min_raw_data";
    private static int min_raw_data_needed;

    /**
     * Minimum coverage by the peaks detected to generate an evidence.
     */
    private static final double[] DEFAULT_COVERAGE = {0.1, 0.5, 0, 1};
    private static FuzzyLogic fuzzy_logic_coverage;

    /**
     * Minimum peak to generate an evidence.
     */
    private static final double THRESHOLD_PARAMETER = 1.3;
    private static final String THRESHOLD_STRING = "threshold_parameter";
    private static double frequency_threshold_parameter;
    private static final double[] DEFAULT_FUZZY_LOGIC_DET = {1.3, 2, 0, 1};
    private static FuzzyLogic fuzzy_logic_threshold;
    //format used for the time started/ended of the agent
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Analyze function inherited from the DetectionAgentInterface.
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

        //get the starting time of the analysis
        Date start_time = new Date();

        RawData[] raw_data = si.findRawData(
                event.getLabel(),
                event.getSubject(),
                event.getTimestamp() - time_window_param,
                event.getTimestamp());

        if (raw_data.length < min_raw_data_needed) {
            return;
        }

        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);

        long[] times = new long[raw_data.length];
        for (int i = 0; i < raw_data.length; i++) {
            times[i] = raw_data[i].getTime();
        }

        long min = min(times);
        long max = max(times);
        long size = pow2gt((max - min) / sampling_detection_interval);

        double[] counts = new double[(int) size];
        for (long time : times) {
            long position = (time - min) / sampling_detection_interval;
            position = Math.min(position, counts.length - 1);
            counts[(int) position]++;
        }

        Complex[] transform
                = fft_transformer.transform(counts, TransformType.FORWARD);

        // Take lower half
        double[] values = new double[transform.length / 2];
        double[] freqs = new double[transform.length / 2];

        for (int i = 0; i < transform.length / 2; i++) {
            freqs[i] = 1.0 / sampling_detection_interval * i / transform.length;
            //values[i] = transform[i].abs();
            // power spectrum
            values[i] = transform[i].multiply(transform[i].conjugate()).abs();
            //        / (raw_data.length * transform.length);
            //System.out.println(values[i]);
        }

        if (values.length < 10) {
            return;
        }

        // Smooth the function
        double[] smoothed_values = smooth(values);
        // get the averages
        double average_values = average(values);
        double average_smoothed_values = average(smoothed_values);

        // Remove DC component
        for (int i = 0; i < 10; i++) {
            values[i] = 0;
            smoothed_values[i] = 0;
        }

        //compute the threshold
        double threshold = computeThreshold(smoothed_values,
                                            average_smoothed_values);
        int min_peak_index = analyzeSpectrumPeaks(smoothed_values, threshold);

        if (min_peak_index == 0) {
            return;
        }

        double min_peak_value = smoothed_values[min_peak_index];

        double min_freq_value = freqs[indexOf(smoothed_values, min_peak_value)];
        double computed_coverage = computeCoverage(times, min_freq_value);
        double msf_result = computeFuzzyLogic(
                min_peak_value / threshold, computed_coverage);

        if (msf_result != 0) {
            //get the end time of the analysis and evidence generation
            Date end_time = new Date();
            //get the parameters used for the detection agent
            HashMap<String, String> parameters = new HashMap<>();
            //add the start and end time to the parameters
            parameters.put("Start Time", DATE_FORMAT.format(
                    start_time));
            parameters.put("End Time", DATE_FORMAT.format(
                    end_time));
            parameters.put("Threshold", Double.toString(threshold));
            parameters.put("Fuzzy Logic Parameters", "Min peak detected: "
                    + min_peak_value + " , "
                    + "Computed Coverage: " + computed_coverage);

            String figure_spectrum_path = createSpectrumFigure(freqs, values,
                    min_freq_value,
                    computeThreshold(values, average_values),
                    event.getSubject().toString(), raw_data[0].getTime());

            String figure_smooth_spectrum_path = createSpectrumFigure(freqs,
                    smoothed_values, min_freq_value,
                    threshold,
                    "smoothing for " + event.getSubject().toString(),
                    raw_data[0].getTime());

            String figure_timeseries_path = createTimeseriesFigure(times,
                    event.getSubject().toString(), raw_data[0].getTime());

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
                    + "with frequency: " + min_freq_value + "Hz "
                    + "| interval: " + Math.round(1 / min_freq_value)
                    + " seconds"
                    + "| score: " + msf_result
                    + figures;

            Evidence evidence = new Evidence();
            evidence.setScore(msf_result);
            evidence.setSubject(event.getSubject());
            evidence.setLabel(dap.getLabel());
            evidence.setTime(raw_data[raw_data.length - 1].getTime());
            evidence.setReport(freq_report);
            si.addEvidence(evidence);
        }
    }

    /**
     * Method for loading all possible parameters from the config *.yaml file.
     * It calls to super.initReportTemplate() to also load the report_template
     * to be used when generating the report.
     * @param profile
     */
    static void initParams(final DetectionAgentProfile profile) {

        //check for parameters set through the config file
        frequency_threshold_parameter = THRESHOLD_PARAMETER;
        try {
            String threshold_string =
                    profile.getParameter(THRESHOLD_STRING);
            if (threshold_string != null) {
                frequency_threshold_parameter =
                        Double.valueOf(threshold_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency detection threshold"
                    + " from configuration file. Error: " + ex.getMessage());
        }

        fuzzy_logic_threshold = new FuzzyLogic(DEFAULT_FUZZY_LOGIC_DET[0],
                                    DEFAULT_FUZZY_LOGIC_DET[1],
                                    DEFAULT_FUZZY_LOGIC_DET[2],
                                    DEFAULT_FUZZY_LOGIC_DET[3]);
        try {
            String fl_x1 = profile.getParameter("fuzzy_logic_det_x1");
            String fl_x2 = profile.getParameter("fuzzy_logic_det_x2");
            String fl_y1 = profile.getParameter("fuzzy_logic_det_y1");
            String fl_y2 = profile.getParameter("fuzzy_logic_det_y2");
            if (fl_x1 != null && fl_y1 != null
                    && fl_x2 != null && fl_y2 != null) {
                fuzzy_logic_threshold = new FuzzyLogic(Double.valueOf(fl_x1),
                                            Double.valueOf(fl_x2),
                                            Double.valueOf(fl_y1),
                                            Double.valueOf(fl_y2));
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency fuzzy logic parameters"
                    + " for the detection threshold "
                    + " from configuration file. Error: " + ex.getMessage());
        }

        fuzzy_logic_coverage = new FuzzyLogic(DEFAULT_COVERAGE[0],
                                    DEFAULT_COVERAGE[1],
                                    DEFAULT_COVERAGE[2],
                                    DEFAULT_COVERAGE[3]);
        try {
            String fl_x1 = profile.getParameter("fuzzy_logic_coverage_x1");
            String fl_x2 = profile.getParameter("fuzzy_logic_coverage_x2");
            String fl_y1 = profile.getParameter("fuzzy_logic_coverage_y1");
            String fl_y2 = profile.getParameter("fuzzy_logic_coverage_y2");
            if (fl_x1 != null && fl_y1 != null
                    && fl_x2 != null && fl_y2 != null) {
                fuzzy_logic_coverage = new FuzzyLogic(Double.valueOf(fl_x1),
                                            Double.valueOf(fl_x2),
                                            Double.valueOf(fl_y1),
                                            Double.valueOf(fl_y2));
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency fuzzy logic parameters"
                    + " for the frequency coverage "
                    + " from configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        min_raw_data_needed = DEFAULT_MIN_RAW_DATA;
        try {
            String threshold_string = profile.getParameter(MIN_RAW_DATA);
            if (threshold_string != null) {
                min_raw_data_needed =
                        Integer.valueOf(threshold_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get fminimum raw data threshold"
                    + " from configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        sampling_detection_interval = SAMPLING_INTERVAL;
        try {
            String sampling_string = profile.getParameter(SAMPLING_STRING);
            if (sampling_string != null) {
                sampling_detection_interval = Integer.valueOf(sampling_string);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency sampling interval from"
                    + " configuration file. Error: " + ex.getMessage());
        }

        //check for parameters set through the config file
        time_window_param = DEFAULT_TIME_WINDOW;
        try {
            String time_window = profile.getParameter(TIME_WINDOW_STRING);
            if (time_window != null) {
                time_window_param = Long.valueOf(time_window);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Could not get frequency time window parameters"
                    + " from configuration file. Error: " + ex.getMessage());
        }
    }

    private double computeFuzzyLogic(final double min_peak,
                                    final double coverage) {
        double mf_result_det =
                fuzzy_logic_threshold.determineMembership(min_peak);
        double mf_result_coverage =
                fuzzy_logic_coverage.determineMembership(coverage);
        //use the FuzzyAnd to see the final result. Any FuzzyAnd method can be
        //used as it is agnostic towards the fuzzy logic object parameters
        return fuzzy_logic_threshold.fuzzyAnd(
            new double[]{mf_result_det, mf_result_coverage});
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

    private double computeThreshold(final double[] values,
                                    final double average) {
        double sigma = 0;
        //compute the sigma
        //sum of the square of the dubstraction of the mean from each value
        for (double value: values) {
            sigma += Math.pow(value - average, 2);
        }
        //divide the sum by the number of values - 1
        sigma = sigma / (double) (values.length - 1);
        //take the square root of the result
        sigma = Math.sqrt(sigma);
        //threshold is equal to the mean + detection_parameter * the sigma
        return average + frequency_threshold_parameter * sigma;
    }

    /**
     * Method for computing the sample coverage for a given period.
     * @param timestamps
     * @param period
     * @return occupied_bins/nr_of_bins
     */
    private double computeCoverage(final long[] timestamps,
                                    final double period) {
        //create the bins for the histogram
        double period_in_seconds = 1 / period;
        int nr_of_bins = (int) ((timestamps[timestamps.length - 1]
                                - timestamps[0]) / period_in_seconds) + 1;
        int[] histogram_bins = new int[nr_of_bins];

        //fill the bins
        for (long timestamp: timestamps) {
            int i_bin = (int) ((timestamp - timestamps[0]) / period_in_seconds);
            if (i_bin < histogram_bins.length) {
                histogram_bins[i_bin] += 1;
            }
        }
        //System.out.println("DEBUG: " + Arrays.toString(histogram_bins));

        //count how many bins are non-empty
        int occupied_bins = 0;
        for (int i = 0; i < histogram_bins.length; i++) {
            if (histogram_bins[i] > 0) {
                occupied_bins += 1;
            }
        }
        //compute and return the ratio of bins that are occupied
        return (double) occupied_bins / nr_of_bins;
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
                            final double average,
                            final String title,
                            final long time)
            throws IOException {

        final XYSeries series_average = new XYSeries("Average");
        series_average.add(freqs[0], average);
        series_average.add(freqs[freqs.length - 1], average);
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

    private void removePeak(final double[] values, final double peak_value) {
        double[] erasure_values = new double[]{0.2, 0.1, 0, 0.1, 0.2};
        int index_of_peak = indexOf(values, peak_value);
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

    private int analyzeSpectrumPeaks(final double[] values,
                                    final double threshold) {

        double[] values_to_analyze = values.clone();
        Map<Double, Integer> max_values = new HashMap<>();
        while (true) {
            double max_peak = max(values_to_analyze);
            if (max_peak > threshold) {
                max_values.put(max_peak, indexOf(values, max_peak));
                removePeak(values_to_analyze, max_peak);
            }
            if (max_peak < threshold) {
                break;
            }
        }
        if (max_values.values().isEmpty()) {
            return 0;
        } else {
            return Collections.min(max_values.values());
        }
    }


}
