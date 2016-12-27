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
import java.util.logging.Logger;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

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

        server = new org.eclipse.jetty.server.Server(config.web_port);

        ServletHandler php_handler = new ServletHandler();
        php_handler.addServletWithMapping(
                com.caucho.quercus.servlet.QuercusServlet.class, "*.php");

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWelcomeFiles(new String[]{"index.php", "index.html"});
        webapp.setBaseResource(Resource.newResource(new File(config.web_root)));
        webapp.setServletHandler(php_handler);
        webapp.setHandler(php_handler);

        server.setHandler(webapp);
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
