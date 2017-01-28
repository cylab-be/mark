package netrank;

import java.io.File;
import mark.activation.DetectionAgentProfile;
import mark.detection.AbstractDetectionAgent;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author Thibault Debatty
 */
public class Frequency extends AbstractDetectionAgent {

    /**
     * Sampling interval (in second).
     */
    public static final int SAMPLING_INTERVAL = 2; // in seconds
    public static final double DETECTION_THRESHOLD = 10.0;

    static int pow2gt(final int value) {
        int result = 1;

        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    static int min(final int[] values) {
        int result = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < result) {
                result = value;
            }
        }
        return result;
    }

    static int max(final int[] values) {
        int result = Integer.MIN_VALUE;
        for (int value : values) {
            if (value > result) {
                result = value;
            }
        }
        return result;
    }

    private double average(double[] values) {
        double result = 0;
        for (double value : values) {
            result += value;
        }

        return result / values.length;
    }

    @Override
    public void analyze(Subject subject, String actual_trigger_label, DetectionAgentProfile profile, ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 20) {
            return;
        }

        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);


        int[] times = new int[raw_data.length];
        for (int i = 0; i < raw_data.length; i++) {
            times[i] = raw_data[i].time;
        }

        int min = min(times);
        int max = max(times);
        int size = pow2gt((max - min) / SAMPLING_INTERVAL);

        double[] counts = new double[size];
        for (int time : times) {
            int position = (time - min) / SAMPLING_INTERVAL;
            counts[position]++;
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
            values[i] = transform[i].multiply(transform[i].conjugate()).abs();
            //        / (raw_data.length * transform.length);
            //System.out.println(values[i]);
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
                    + "= " + (1/freqs[i]) + " seconds\n";

            datastore.addEvidence(evidence);
        }

        final XYSeries series = new XYSeries("Data");
        for (int i = 0; i < freqs.length; i++) {
            series.add(freqs[i], values[i]);
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "XY Series Demo",
                "Frequency (Hz)",
                "Y",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartUtilities.saveChartAsPNG(
                File.createTempFile("frequency_chart", ".png"),
                chart, 1600, 1200);
    }
}
