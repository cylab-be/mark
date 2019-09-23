/*
 * The MIT License
 *
 * Copyright 2019 tibo.
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
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import static spark.Spark.halt;
import spark.TemplateViewRoute;

/**
 *
 * @author tibo
 */
class StatusRoute implements TemplateViewRoute {

    private final Client client;

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(StatusRoute.class);

    public StatusRoute(final Client client) {
        this.client = client;
    }

    @Override
    public ModelAndView handle(final Request rqst, final Response rspns)
            throws Exception {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("mark", this.client);
        try {
            attributes.put("status", this.client.status());
            attributes.put("executor", this.client.executorStatus());
            attributes.put("dbstatus", this.client.dbStatus());
        } catch (Throwable ex) {
            LOGGER.error("Failed to read from client: " + ex.getMessage());
            halt(500);
        }
        return new ModelAndView(attributes, "status.html");
    }
}
