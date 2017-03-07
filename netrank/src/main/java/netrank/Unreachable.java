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
 * Agent responsible for analyzing the connection status to a server.
 * Determines the # of bad connections compared to the # good connections.
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

        int number_of_unreachable = 0;
        float unreachable_percentage = 0;

        for (int u = 0; u < status_array.length; u++) {
            if (doesContain(status_array[u])) {
                number_of_unreachable = number_of_unreachable + 1;
            }
        }

        if (number_of_unreachable != 0) {
            unreachable_percentage = (float) number_of_unreachable
                    / status_array.length;
        } else {
            unreachable_percentage = 0;
        }

        if (unreachable_percentage > 0.2) {
            Evidence evidence = new Evidence();
            evidence.score = unreachable_percentage;
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found a periodicity in the "
                    + "with unreachable periodicity: "
                    + unreachable_percentage + "\n";

            datastore.addEvidence(evidence);
        }
    }
}