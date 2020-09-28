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
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
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
public class ReportDataRoute implements TemplateViewRoute {

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(ReportRoute.class);

    private final Client client;

    /**
     * Shows data entries.
     *
     * @param client
     */
    public ReportDataRoute(final Client client) {
        this.client = client;
    }

    /**
     * Shows data entries.
     *
     * @param rqst
     * @param rspns
     * @return
     * @throws Exception
     */
    @Override
    public final ModelAndView handle(final Request rqst, final Response rspns)
            throws Exception {
        String report_id = rqst.params(":id");
        int data_rqst_id = Integer.valueOf(rqst.params(":rq"));

        Map<String, Object> model = new HashMap<>();
        try {
            Evidence ev = client.findEvidenceById(report_id);
            model.put("report", ev);

            String query = (String) ev.getRequests().get(data_rqst_id);
            model.put("query", query);

            RawData[] data = client.findData(query);
            model.put("data", data);


        } catch (Throwable ex) {
            LOGGER.error("Failed to get report from datastore : "
                    + ex.getMessage());
            halt(404);
        }

        return new ModelAndView(model, "reportdata.html");
    }
}
