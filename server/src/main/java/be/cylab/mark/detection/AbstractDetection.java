package be.cylab.mark.detection;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import be.cylab.mark.core.DetectionAgentInterface;
import java.net.URL;


abstract class AbstractDetection implements DetectionAgentInterface{
    protected final String make_report(Object[] obj) throws Throwable{
        JsonRpcHttpClient jrc = new JsonRpcHttpClient(new URL(System.getenv("REPORT_MAKER_HOST")));
        return jrc.invoke(this.getClass().getTypeName(),  obj, String.class);
    }
    protected final String make_report() throws Throwable{
        return make_report(new Object[]{});
    }
    protected final String make_report(Object obj) throws Throwable{
        return make_report(new Object[]{obj});
    }
}