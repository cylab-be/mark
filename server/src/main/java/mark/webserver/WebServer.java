/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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
package mark.webserver;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mark.server.Config;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
public class WebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            WebServer.class);

    private Server server;
    private final Config config;

    /**
     * Instantiate a web server with provided config.
     * @param config
     */
    public WebServer(final Config config) {
        this.config = config;
    }

    /**
     * Start the file server.
     *
     * @throws java.lang.Exception if jetty fails to start
     */
    public final void start() throws Exception {
        if (!config.start_webserver) {
            return;
        }

        LOGGER.info("Starting web interface at port 8000");

        // Handle php files
        ServletContextHandler php_handler = new ServletContextHandler();
        php_handler.addServlet(
                com.caucho.quercus.servlet.QuercusServlet.class, "*.php");
        php_handler.setResourceBase(config.webserver_root);

        // Handle static files (if it's not php)
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase(config.webserver_root);

        HandlerList handler_list = new HandlerList();
        handler_list.setHandlers(new Handler[]{
            php_handler,
            resource_handler
        });

        // First of all, redirect to index.php if file does not exist
        RewriteHandler rewrite_handler = new RewriteHandler();
        rewrite_handler.setRewriteRequestURI(false);
        rewrite_handler.setRewritePathInfo(false);
        rewrite_handler.setOriginalPathAttribute("requestedPath");
        rewrite_handler.addRule(new RewriteIfNotExistsRule(config.webserver_root));
        rewrite_handler.setHandler(handler_list);

        server = new Server(config.webserver_port);
        server.setHandler(rewrite_handler);
        server.start();
    }

    /**
     *
     * @throws Exception
     */
    public final void stop() throws Exception {
        if (server == null) {
            return;
        }
        
        server.stop();
    }
}

/**
 * Rewrite the request if the requested file does not exist.
 * Similar to following Apache .htaccess:
 * RewriteEngine On
 * RewriteCond %{REQUEST_FILENAME} !-f
 * RewriteRule ^(.*)$ index.php [QSA,L]
 *
 * Or to following nginx:
 * server {
 *   location / {
 *     try_files $uri /index.php;
 *   }
 * }
 * @author Thibault Debatty
 */
class RewriteIfNotExistsRule extends Rule {
    private final String root;

    RewriteIfNotExistsRule(final String root) {
        this.root = root;
        _terminating = false;
        _handling = false;
    }

    @Override
    public String matchAndApply(
            final String target,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        File resource = new File(root + target);
        if (resource.exists() && resource.isFile()) {
            return target;
        }

        return "/index.php";
    }
}