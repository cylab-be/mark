package be.cylab.mark.detection;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import be.cylab.mark.core.DetectionAgentInterface;
import java.net.URL;


abstract class AbstractDetection implements DetectionAgentInterface{
    protected final String make_report(Object[] obj){
        try {
            JsonRpcHttpClient jrc = new JsonRpcHttpClient(new URL(System.getenv("REPORT_MAKER_HOST")));
            return jrc.invoke(this.getClass().getTypeName(),  obj, String.class);
        } catch (Throwable t){
            return new String("Unable to connect to Report Server");
        }
    }
    protected final String make_report(){
        return make_report(new Object[]{});
    }
    protected final String make_report(Object obj){
        return make_report(new Object[]{obj});
    }
}