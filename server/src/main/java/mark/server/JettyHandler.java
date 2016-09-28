package mark.server;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Thibault Debatty
 */
class JettyHandler extends AbstractHandler {
    private final JsonRpcServer jsonrpc_server;

    JettyHandler(JsonRpcServer jsonrpc_server) {
        this.jsonrpc_server = jsonrpc_server;
    }

    /**
     *
     * @param target
     * @param base_request
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void handle(
            String target,
            Request base_request,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        jsonrpc_server.handle(request, response);
    }

}
