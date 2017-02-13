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
import mark.core.DataAgentInterface;
import mark.core.DataAgentProfile;
import mark.core.ServerInterface;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {
        Server server = new Server(8080);

        HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);

        ServletHandler servlet_handler = new ServletHandler();
        servlet_handler.addServletWithMapping(
                new ServletHolder(new MyProxyServlet(datastore)), "/*");
        handlers.addHandler(servlet_handler);

        // Setup proxy handler to handle CONNECT methods
        ConnectHandler proxy = new ConnectHandler();
        handlers.addHandler(proxy);

        RequestLogHandler log_handler = new RequestLogHandler();
        log_handler.setRequestLog(new MyLogger());
        handlers.addHandler(log_handler);

        //server.setHandler(servletHandler);
        server.start();
        server.join();

    }

    /**
     * Proxies the received requests.
     */
    public static final class MyProxyServlet extends ProxyServlet {
        private final ServerInterface datastore;

        private MyProxyServlet(final ServerInterface datastore) {
            this.datastore = datastore;
        }

        @Override
        public void init(final ServletConfig config) throws ServletException {
            super.init(config);
            System.out.println(">> init done !");
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
    private static class MyLogger extends AbstractNCSARequestLog {

        MyLogger() {
            setExtended(true);
            setLogLatency(true);
        }

        @Override
        protected boolean isEnabled() {
            return true;
        }

        @Override
        public void write(final String string) throws IOException {
            System.out.println(string);
        }

    }
}
