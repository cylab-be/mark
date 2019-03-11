package netrank;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
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
public class Unreachable implements DetectionAgentInterface<Link> {

    private final int[] bad_server_status = {500, 501, 502, 503, 504};
    private static final double DEFAULT_UNREACHABLE_THRESHOLD = 0.03;
    private static final String THRESHOLD_STRING = "threshold";

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

    /**
     * Method for generating a graph from the data recovered from the database.
     * @param dataset
     * @param title
     * @param time
     * @throws IOException
     */
    private String createGraph(final Map dataset,
                            final String title,
                            final long time)
            throws IOException {

        String path = "";

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
                "Unreachable (Client:Server) " + title, //Title
                "Time", //Category X-axis
                " ", //Category Y-axis
                cat_dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );
            //transform timestamp to create day folder for the graphs
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(time);
            String formated_date = sf.format(date);
            //set the axis data to be visible or not
            CategoryPlot plot = chart.getCategoryPlot();
            plot.getRangeAxis().setVisible(false);
            plot.getDomainAxis().setVisible(true);
            //create the folders to store the graph
            File graph_path = new File("/tmp/mark_figures/"
                                            + formated_date
                                            + "/");
            graph_path.mkdirs();
            //create the temporary graph file
            File graph = File.createTempFile("unreachable_chart", ".png",
                        graph_path);
            //load the chart into the graph file
            ChartUtilities.saveChartAsPNG(graph, chart, 1920, 1080);
            path = graph.getAbsolutePath();
            }
        return path;
    }

    // Analyze function inherited from the DetectionAgentInterface
    // accepts the subject to analyze
    // trigger of the agent
    // the profile used to load the agent
    // the database to which to connect to gather RawData
    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        //check for parameters set through the config file
        double threshold = DEFAULT_UNREACHABLE_THRESHOLD;
        String threshold_string = profile.parameters.get(THRESHOLD_STRING);
        if (threshold_string != null) {
            try {
                threshold = Double.valueOf(threshold_string);
            } catch (NumberFormatException ex) {
                threshold = DEFAULT_UNREACHABLE_THRESHOLD;
            }
        }

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        if (raw_data.length < 10) {
            return;
        }

        //Count the # of unreachable connections discovered.
        //Discovering unreachable connections by checking a pattern looking for
        //the server status 50*
        int number_of_unreachable = 0;
        Map graph_dataset = new HashMap();
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
                graph_dataset.put(timestamp, false);
            } else {
                graph_dataset.put(timestamp, true);
            }
        }

        float unreachable_percentage = 0;

        unreachable_percentage = (float) number_of_unreachable
                / raw_data.length;

        if (unreachable_percentage > threshold) {

            String graph_path = createGraph(graph_dataset,
                    subject.toString(),
                    raw_data[0].time);

            Evidence evidence = new Evidence();
            evidence.score = unreachable_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a connection between: "
                    + "<br /> client " + subject.getClient()
                    + "and server " + subject.getServer()
                    + "<br />where the number of times the server was "
                    + "unreachable is above the allowed threshold"
                    + "<br />Ratio of unreachable connections: "
                    + unreachable_percentage
                    + "<br />Number of entries analysed: " + raw_data.length
                    + "<br />The Unreachable threshold ratio used is: "
                    + threshold
                    + "<br />Graph: "
                    + "<a href=\"file:///" + graph_path
                    + "\">" + "Unreachable Graph</a>";

            datastore.addEvidence(evidence);
        }
    }
}
