package be.cylab.mark.detection;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import be.cylab.mark.core.DetectionAgentInterface;
import java.net.URL;


abstract class AbstractDetection implements DetectionAgentInterface{
    protected final String make_report(Object[] obj) throws Throwable{
        JsonRpcHttpClient jrc = new JsonRpcHttpClient(new URL(System.getenv("REPORT_MAKER_HOST")));
        String name = this.getClass().getTypeName();
        return jrc.invoke(name,  obj, String.class);
    }
    protected final String make_report() throws Throwable{
        return make_report(new Object[]{});
    }
}