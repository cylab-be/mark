/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.core.DataAgentInterface;
import mark.core.DataAgentProfile;
import mark.core.RawData;
import mark.core.ServerInterface;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 *
 * @author Thibault Debatty
 */
public class Proxy implements DataAgentInterface {

    /**
     *
     * @param args
     * @throws Throwable if something went wrong
     */
    public static void main(final String[] args) throws Throwable {
        Proxy proxy = new Proxy();
        proxy.run(null, null);

    }

    /**
     *
     * @param profile
     * @param datastore
     * @throws Throwable if something went wrong...
     */
    @Override
    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {
        Server server = new Server(3128);

        HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);

        ServletHolder servlet_holder = new ServletHolder(new MyProxyServlet());
        servlet_holder.setInitParameter("maxThreads", "8");

        ServletHandler servlet_handler = new ServletHandler();
        servlet_handler.addServletWithMapping(servlet_holder, "/*");
        handlers.addHandler(servlet_handler);

        // Setup proxy handler to handle CONNECT methods
        ConnectHandler proxy = new ConnectHandler();
        handlers.addHandler(proxy);

        RequestLogHandler log_handler = new RequestLogHandler();
        log_handler.setRequestLog(new MyLogger(profile, datastore));
        handlers.addHandler(log_handler);

        //server.setHandler(servletHandler);
        server.start();
        server.join();
    }

    /**
     * Proxies the received requests.
     */
    public static final class MyProxyServlet extends ProxyServlet {

        @Override
        public void init(final ServletConfig config) throws ServletException {
            super.init(config);
        }

        @Override
        public void service(final ServletRequest req, final ServletResponse res)
                throws ServletException, IOException {
            super.service(req, res);
        }

        @Override
        protected HttpClient newHttpClient() {
            return new HttpClient(new SslContextFactory());
        }
    }

    /**
     * Special logger that will write requests to Datastore.
     */
    private static final class MyLogger extends AbstractLifeCycle
            implements RequestLog {

        private final ServerInterface datastore;
        private DataAgentProfile profile;

        private MyLogger(
                final DataAgentProfile profile,
                final ServerInterface datastore) {
            this.datastore = datastore;
            this.profile = profile;
        }

        @Override
        public void log(final Request request, final Response response) {
            HTTPLogLine log_line = new HTTPLogLine(request, response);

            RawData<Link> data = new RawData<>();
            data.label = profile.label;
            data.subject = new Link(
                    log_line.getRemoteAddress(),
                    log_line.getServerName());
            data.time = (int) (log_line.getTime() / 1000);
            data.data = log_line.toString();
            try {
                datastore.addRawData(data);
            } catch (Throwable ex) {
                Logger.getLogger(
                        Proxy.class.getName()).log(
                                Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}

/**
 * Represents a single line in a http request log file.
 * @author Thibault Debatty
 */
class HTTPLogLine {
    private final String server_name;
    private final int status;
    private final long content_length;
    private final String protocol;
    private final String uri;
    private final String method;
    private final long time;
    private final String remote_address;

    HTTPLogLine(final Request request, final Response response) {
        this.server_name = request.getServerName();
        this.remote_address = request.getRemoteAddr();
        this.time = request.getTimeStamp();
        this.method = request.getMethod();
        this.uri = request.getUri().toString();
        this.protocol = request.getProtocol();
        this.content_length = response.getContentLength();
        this.status = response.getStatus();
        //System.out.println(response.getHeaderNames());
    }

    /**
     *
     * @return
     */
    public String getServerName() {
        return server_name;
    }

    /**
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     *
     * @return
     */
    public long getContentLength() {
        return content_length;
    }

    /**
     *
     * @return
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     *
     * @return
     */
    public String getUri() {
        return uri;
    }

    /**
     *
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     *
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     *
     * @return
     */
    public String getRemoteAddress() {
        return remote_address;
    }

    /**
     *
     * @return
     */
    public String toString() {
        return ""
                + this.time / 1000
                + " - "
                + remote_address + " "
                + status + " "
                + content_length + " "
                + method + " "
                + uri;

    }
}
