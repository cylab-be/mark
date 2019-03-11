package netrank;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
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
public class Frequency implements DetectionAgentInterface<Link> {

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
     * Method for generating a graph from the data recovered from the database.
     * @param dataset
     * @param title
     * @throws IOException
     */
    private String createGraph(final double[] freqs,
                            final double[] values,
                            final String title,
                            final long time)
            throws IOException {

        final XYSeries series = new XYSeries("Data");
        for (int n = 0; n < freqs.length; n++) {
            series.add(freqs[n], values[n]);
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Frequency between (Client:Server) " + title,
                "Frequency (Hz)",
                "Y",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        //transform timestamp to create day folder for the graphs
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(time);
                String formated_date = sf.format(date);
                //create the folders to store the graph
                File graph_path = new File("/tmp/mark_figures/"
                                            + formated_date
                                            + "/");
                graph_path.mkdirs();
                //create the temporary graph file
                File graph = File.createTempFile("frequency_chart", ".png",
                                graph_path);
                //load the chart into the graph file
                ChartUtilities.saveChartAsPNG(graph, chart, 1600, 1200);
                return graph.getAbsolutePath();
    }

    @Override
    public final void analyze(
            final Link subject,
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
            String graph_path = createGraph(freqs, values,
                    subject.toString(),
                    raw_data[0].time);

            Evidence evidence = new Evidence();
            evidence.score = 0.9;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found peak for frequency between: "
                    + "<br /> client " + subject.getClient()
                    + " and server " + subject.getServer()
                    + "<br />with frequency " + freqs[i] + "\n"
                    + "= " + (1 / freqs[i]) + " seconds\n"
                    + "<br />"
                    + "<br />Number of entries analysed: " + raw_data.length
                    + "<br />Graph: "
                    + "<a href=\"file:///" + graph_path
                    + "\">" + "Frequency Graph</a>";

            datastore.addEvidence(evidence);
        }
    }
}
