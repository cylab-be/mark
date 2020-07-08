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
package be.cylab.mark.webserver;

import be.cylab.mark.client.Client;
import com.google.inject.Inject;
import be.cylab.mark.server.Config;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.template.pebble.PebbleTemplateEngine;

/**
 *
 * @author Thibault Debatty
 */
public class WebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            WebServer.class);

    private final Config config;

    /**
     * Instantiate a web server with provided config.
     *
     * @param config
     */
    @Inject
    public WebServer(final Config config) {

        this.config = config;
    }

    /**
     * Start the web server (non-blocking).
     *
     * @throws java.lang.Exception if jetty fails to start
     */
    public final void start() throws Exception {
        if (!config.start_webserver) {
            return;
        }

        LOGGER.info("Starting web interface at port 8000");

        Client client = new Client(
                config.getDatastoreUrl(), config.getSubjectAdapter());


        PebbleTemplateEngine pebble = new PebbleTemplateEngine(
                new ClasspathLoader());

        spark.Spark.staticFiles.location("/static");
        spark.Spark.port(8000);

        spark.Spark.get("/", new HomeRoute(client), pebble);
        spark.Spark.get("/status/pause", (rqst, rspns) -> {
            try {
                client.pause();
            } catch (Throwable ex) {
                LOGGER.error("Failed to pause server!: " + ex.getMessage());
            }
            rspns.redirect("/status");
            return null;
        });

        spark.Spark.get("/status/resume", (rqst, rspns) -> {
            try {
                client.resume();
            } catch (Throwable ex) {
                LOGGER.error("Failed to resume server!: " + ex.getMessage());
            }
            rspns.redirect("/status");
            return null;
        });

        spark.Spark.get("/status", new StatusRoute(client), pebble);
        spark.Spark.get("/report/:id/data/:rq", new ReportDataRoute(client), pebble);
        spark.Spark.get("/report/:id", new ReportRoute(client), pebble);
    }

    /**
     *
     * @throws Exception if an error happens while stopping the server
     */
    public final void stop() throws Exception {
        spark.Spark.stop();
    }
}