package be.cylab.mark.detection;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import java.net.URL;


/**
 * An Abstract class for Dectection Agent providing the communication
 * with Report Maker Engine.
 */
class ReportPythonClient {
    protected final String makeReport(final Object[] obj) {
        try {
            JsonRpcHttpClient jrc = new JsonRpcHttpClient(
                    new URL(System.getenv("REPORT_MAKER_HOST")));
            return jrc.invoke(this.getClass().getTypeName(), obj, String.class);
        } catch (Throwable t) {
            return "Unable to connect to Report Server";
        }
    }
    protected final String makeReport(final Object obj) {
        return makeReport(new Object[] {obj});
    }
}
