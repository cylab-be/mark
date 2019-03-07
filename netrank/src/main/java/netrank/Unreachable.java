package netrank;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Agent responsible for analyzing the connection status to a server.
 * Determines the # of bad connections compared to the # good connections.
 * Bad connections are of the type [50*].
 * @author Georgi Nikolov
 */
public class Unreachable implements DetectionAgentInterface {

    private final int[] bad_server_status = {500, 501, 502, 503, 504};

    //Private function to determine if a status shows a bad connection
    //to the server
    private boolean doesContain(final int status) {
        boolean result = false;
        for (int i = 0; i < bad_server_status.length; i++) {
            if (status == bad_server_status[i]) {
                result = true;
            }
        }
        return result;
    }

    // Analyze function inherited from the DetectionAgentInterface
    // accepts the subject to analyze
    // trigger of the agent
    // the profile used to load the agent
    // the database to which to connect to gather RawData
    @Override
    public final void analyze(
            final Subject subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 10) {
            return;
        }

        //Count the # of unreachable connections discovered.
        //Discovering unreachable connections by checking a pattern looking for
        //the server status 50*
        int number_of_unreachable = 0;
        Map dataset = new HashMap();
        Pattern pattern = Pattern.compile("/" + "([0-9]{3})\\s");
        for (RawData raw_data1 : raw_data) {
            int status = 0;
            long timestamp = (long) raw_data1.time;
            Matcher matcher = pattern.matcher(raw_data1.data);
            if (matcher.find()) {
                status = Integer.parseInt(matcher.group(1));
            }
            if (doesContain(status)) {
                number_of_unreachable = number_of_unreachable + 1;
                dataset.put(timestamp, false);
            } else {
                dataset.put(timestamp, true);
            }
        }

        float unreachable_percentage = 0;

        unreachable_percentage = (float) number_of_unreachable
                / raw_data.length;

        if (unreachable_percentage > 0) {
            Evidence evidence = new Evidence();
            evidence.score = unreachable_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found an unreachable server with ratio: "
                    + unreachable_percentage + "\n";

            datastore.addEvidence(evidence);

            if (!dataset.isEmpty()) {
                DefaultCategoryDataset cat_dataset =
                        new DefaultCategoryDataset();
                for (Object key: dataset.keySet()) {
                    boolean value = (boolean) dataset.get(key);
                    String dat_key = String.valueOf(key);
                    if (value) {
                        cat_dataset.addValue(0, "Unreachable", dat_key);
                        cat_dataset.addValue(1, "Reachable", dat_key);
                    } else {
                        cat_dataset.addValue(1, "Unreachable", dat_key);
                        cat_dataset.addValue(0, "Reachable", dat_key);
                    }
                }
                JFreeChart chart = ChartFactory.createBarChart(
                    "Unreachable (Client:Server) " + subject.toString(), //Title
                    "Time", //Category X-axis
                    " ", //Category Y-axis
                    cat_dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
                );
                CategoryPlot plot = chart.getCategoryPlot();
                plot.getRangeAxis().setVisible(false);
                plot.getDomainAxis().setVisible(false);
                File figure = new File("/tmp/mark_figures/");
                figure.mkdirs();
                ChartUtilities.saveChartAsPNG(
                    File.createTempFile("unreachable_chart", ".png",
                            figure),
                    chart, 1920, 1080);
            }
        }
    }
}
