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
package mark.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 *
 * @author Thibault Debatty
 */
public class FileServer {

    private static final Logger LOGGER
            = Logger.getLogger(FileServer.class.getName());

    private org.eclipse.jetty.server.Server server;
    private final Config config;

    /**
     * Instantiate a web server with provided config.
     * @param config
     */
    public FileServer(final Config config) {
        this.config = config;
    }

    /**
     * Start the file server (blocking).
     *
     * @throws Exception if server cannot start
     */
    public final void start() throws Exception {
        LOGGER.info("Starting web interface at port 8000");

        // Handle php files
        ServletContextHandler php_handler = new ServletContextHandler();
        php_handler.addServlet(
                com.caucho.quercus.servlet.QuercusServlet.class, "*.php");
        php_handler.setResourceBase(config.web_root);

        // Handle static files (if it's not php)
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase(config.web_root);

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
        rewrite_handler.addRule(new RewriteIfNotExistsRule(config.web_root));
        rewrite_handler.setHandler(handler_list);

        server = new org.eclipse.jetty.server.Server(config.web_port);
        server.setHandler(rewrite_handler);
        server.start();
    }

    /**
     * Wait for the server to stop.
     * @throws Exception if Jetty goes wrong...
     */
    public final void stop() throws Exception {
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