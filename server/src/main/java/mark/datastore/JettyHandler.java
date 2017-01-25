package mark.datastore;

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

    JettyHandler(final JsonRpcServer jsonrpc_server) {
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
            final String target,
            final Request base_request,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {

        response.addHeader("Access-Control-Allow-Origin", "*");

        jsonrpc_server.handle(request, response);
    }

}
