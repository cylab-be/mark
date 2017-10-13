package netrank;

import java.io.File;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Thibault Debatty
 */
public class Frequency implements DetectionAgentInterface {

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

    @Override
    public final void analyze(
            final Subject subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 50) {
            return;
        }

        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);


        long[] times = new long[raw_data.length];
        for (int i = 0; i < raw_data.length; i++) {
            times[i] = raw_data[i].time;
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
            values[i] = transform[i].multiply(transform[i].conjugate()).abs();
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
                    + "= " + (1 / freqs[i]) + " seconds\n";

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
