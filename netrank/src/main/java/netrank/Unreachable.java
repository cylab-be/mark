package netrank;

//import java.io.File;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
//import org.apache.commons.math3.complex.Complex;
//import org.apache.commons.math3.transform.DftNormalization;
//import org.apache.commons.math3.transform.FastFourierTransformer;
//import org.apache.commons.math3.transform.TransformType;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
import java.util.regex.*;

/**
 *
 * @author Georgi Nikolov
 */
public class Unreachable implements DetectionAgentInterface {

    /**
     * Maybe add Sampling, not sure if needed or not.
     */

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

    private double average(final double[] values) {
        double result = 0;
        for (double value : values) {
            result += value;
        }

        return result / values.length;
    }
    
    private int checkPeriodicity(final int[][] values) {
        int result = 0;
        return result;
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

        int[][] times_status = new int[raw_data.length][2];
        int[] status_array = new int[raw_data.length];
        for (int i = 0; i < raw_data.length; i++) {
            int timestamp = raw_data[i].time;
            int status = 0;
 //           times_status[i] = raw_data[i].time;
            Pattern pattern = Pattern.compile(".*TCP_MISS/([0-9]{3}).*");
            Matcher matcher = pattern.matcher(raw_data[i].data);
            if (matcher.find()) {
                status = Integer.parseInt(matcher.group(1));
            }
            int[] time_status = {timestamp, status};
            times_status[i] = time_status;
            status_array[i] = status;
        }
        
        int good_connections = 0;
        for (int n= 0; n < status_array.length; n++) {
            if (status_array[n] == 200) {
                good_connections = good_connections + 1;
            }
        }
        
        float good_connection_percentage = 0;
        if (good_connections == 0) {
            return;
        } else {
            good_connection_percentage = (good_connections / 
                    status_array.length) * 100;
        }
        
        int unreachable_periodicity = checkPeriodicity(times_status);
        
        if (unreachable_periodicity > 0.5) {
            Evidence evidence = new Evidence();
            evidence.score = 0.9;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a periodicity in the "
                    + "unreachable percentage: "
                    + unreachable_periodicity + "\n";

            datastore.addEvidence(evidence);
        }
    }
}