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
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class HomeRoute implements TemplateViewRoute {

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(HomeRoute.class);

    private final Client client;

    public HomeRoute(Client client) {
        this.client = client;
    }

    @Override
    public ModelAndView handle(Request rqst, Response rspns) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        String label = "detection.counter";
        try {
            String[] labels = this.getLabels();
            LOGGER.info("Found " + labels.length + " labels");
            attributes.put("labels", this.getLabels());

            label = rqst.queryParamOrDefault(
                    "label",
                    labels[0]);
            Evidence[] evidences = this.client.findEvidence(label);
            LOGGER.info("Found " + evidences.length + " evidences");
            attributes.put("evidences", evidences);
        } catch (Throwable ex) {
            LOGGER.error("Failed to read from datastore!", ex);
            halt(505);
        }
        return new ModelAndView(attributes, "index.html");
    }

    private String[] getLabels() throws Throwable {
        List<String> labels = new ArrayList<>();

        //Map status = this.client.status();
        //System.out.println(status);
        //List<DetectionAgentProfile> profiles =
        //        (LinkedList<DetectionAgentProfile>) status.get("activation");
        DetectionAgentProfile[] profiles = this.client.activation();

        for (DetectionAgentProfile profile : profiles) {
            labels.add(profile.getLabel());
        }

        return labels.toArray(new String[labels.size()]);
    }
}
