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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Georgi Nikolov
 * Agent tests the ratio of POST bytes sent during the client-server connection.
 * if the ratio exceeds a specified threshold it is tagged as suspicious.
 */
public class Upload implements DetectionAgentInterface<Link> {

    private static final double UPLOAD_THRESHOLD = 0.5;
    private final Map dataset = new HashMap();
    /**
     * method for calculating the ratio between the bytes sent via POST method.
     * and the total amount of bytes Sent/Received
     * during a client-server connection
     * @param rawdata
     * @return post_bytes_ratio
     */
    private double postBytesSentRatio(final RawData[] raw_data) {
        double post_bytes_ratio = 0;
        long post_bytes = 0;
        long all_bytes = 0;
        // patern for searching for numbers surrounded by whitespace
        Pattern pattern = Pattern.compile("\\s(\\d+)\\s[GET|POST|CONNECT]");
        for (RawData line : raw_data) {

            Matcher matcher = pattern.matcher(line.data);
            if (!matcher.find()) {
                continue;
            }

            long bytes = Integer.parseInt(matcher.group(1));
            // if the method is post increment the total bytes posted with
            // the value retrieved
            if (line.data.contains(" POST ")) {
                post_bytes += bytes;
                dataset.put(line.time, bytes);
            } else {
                dataset.put(line.time, new Long(0));
            }
            all_bytes += bytes;
        }
        // check that we don't divide by 0
        if (all_bytes != 0) {
            post_bytes_ratio = (double) post_bytes / all_bytes;
        }
        return post_bytes_ratio;
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
                final XYSeries series = new XYSeries("Data");
                for (Object key: dataset.keySet()) {
                    long dat_key = (long) key;
                    long dat_value = (long) dataset.get(key);
                    series.add(dat_key, dat_value);
                }
                final XYSeriesCollection data = new XYSeriesCollection(series);
                final JFreeChart chart = ChartFactory.createXYLineChart(
                        "POST Bytes between (Client:Server) "
                                + title,
                        "Timestamp",
                        "Bytes Sent",
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
                File graph = File.createTempFile("upload_chart", ".png",
                                graph_path);
                //load the chart into the graph file
                ChartUtilities.saveChartAsPNG(graph, chart, 1600, 1200);
                path = graph.getAbsolutePath();
        }
        return path;
    }

    /**
     * Analyze function inherited from the DetectionAgentInterface.
     * accepts the subject to analyze
     * trigger of the agent
     * the profile used to load the agent
     * the database to which to connect to gather RawData
     * @throws java.lang.Throwable
     */
    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);

        if (raw_data.length < 20) {
            return;
        }

        double post_percentage = postBytesSentRatio(raw_data);

        if (post_percentage > UPLOAD_THRESHOLD) {

            String graph_path = createGraph(dataset,
                    subject.toString(),
                    raw_data[0].time);

            Evidence evidence = new Evidence();
            evidence.score = post_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a ratio of POST methods in the "
                    + "connection between " + subject.getClient()
                    + " and " + subject.getServer()
                    + " with suspicious post ratio of : "
                    + post_percentage + "\n <br />"
                    + "Graph: "
                    + "<a href=\"file:///" + graph_path
                    + "\">" + "Upload Graph</a>";

            datastore.addEvidence(evidence);
        }
    }
}
