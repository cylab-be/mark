package netrank;

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Georgi Nikolov
 */
public class Unreachable implements DetectionAgentInterface {

    /**
     * Maybe add Sampling, not sure if needed or not.
     */

    private double checkPeriodicity(final int[] values) {
        //Check the percentage of unreachable connections
        int nmb_unreachable_connections = 0;
        int percentage_unreachable_connections = 0;
        double result = 1;
        for (int n = 0; n < values.length; n++) {
            int status = values[n];
            if (status == 400) {
                nmb_unreachable_connections = nmb_unreachable_connections + 1;
            }
        }

        if (nmb_unreachable_connections == 0) {
            return 0;
        } else {
            percentage_unreachable_connections = 1
                    - (nmb_unreachable_connections / values.length);
        }
        //If more than half the connections are unreachable check how the
        //connection statuses follow each other
        if (percentage_unreachable_connections > 0.4) {

        //Check how the statuses follow each other, is it a constant switch
        //between a reachable and unreachable status or a large time periods
        //with connection to the server followed by an unreachable period

            int previous_status = 0;
            for (int i = 0; i < values.length; i++) {
                int status = values[i];
                if (previous_status == 0) {
                    previous_status = status;
                } else {
                    if (previous_status == status) {
                        result = result - 0.1;
                    }
                }
                previous_status = status;
            }
        }
        if (result < 0) {
            result = 0;
        }
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
            System.out.println("No Raw Data Given \n");
            return;
        }

        int[][] times_status = new int[raw_data.length][2];
        int[] status_array = new int[raw_data.length];
        Pattern pattern = Pattern.compile(".*TCP_MISS/([0-9]{3}).*");
        for (int i = 0; i < raw_data.length; i++) {
            int timestamp = raw_data[i].time;
            int status = 0;
            Matcher matcher = pattern.matcher(raw_data[i].data);
            if (matcher.find()) {
                status = Integer.parseInt(matcher.group(1));
            }
            int[] time_status = {timestamp, status};
            times_status[i] = time_status;
            status_array[i] = status;
        }

        double unreachable_periodicity = checkPeriodicity(status_array);

        if (unreachable_periodicity > 0.5) {
            Evidence evidence = new Evidence();
            evidence.score = 0.9;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a periodicity in the "
                    + "with unreachable periodicity: "
                    + unreachable_periodicity + "\n";

            datastore.addEvidence(evidence);
        }
    }
}
