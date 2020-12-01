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

package be.cylab.mark.detection;

import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.ServerInterface;
import info.debatty.java.aggregation.WOWA;

import java.util.Map;
import java.util.TreeMap;


/**
 *
 * @author Alexandre Croix
 * @author Thibault Debatty
 */
public class WOWAgregation implements DetectionAgentInterface {

    private static final double[] DEFAULT_P_WEIGHTS = {0.2, 0.4, 0.3, 0.1};
    private double[] w_weights;
    private double[] p_weights;
    private int evidences_number;

    /**
     * Initialize parameters.
     * Take parameters in config yml file
     * Or use default parameters if not correct.
     *
     * Put weights agents parameters in label alphabetic order.
     * Default W weight are 1 / evidence number
     * Default P weights are {0.2, 0.4, 0.3, 0.1, 0.0,.....}
     * @param profile
     */
    final void initParams(final DetectionAgentProfile profile) {
        String w_weights_string = null;
        String p_weights_string = null;
        try {
            w_weights_string = profile.getParameter("w_weights");
            p_weights_string = profile.getParameter("p_weights");

        } catch (Exception ex) {
            System.out.println("Could not get the WOWA weight parameters"
                    + " from configuration file. Error: " + ex.getMessage());
        }
        if (checkDefaultParameters(w_weights_string)) {
            w_weights = generateDefaultWVector();
            //w_weights = normalizeVector(w_weights);
        } else {
            w_weights = parseDoubleArray(w_weights_string);
            w_weights = normalizeVector(w_weights);
        }
        if (checkDefaultParameters(p_weights_string)) {
            p_weights = generateDefaultPVector();
            p_weights = normalizeVector(p_weights);
        } else {
            p_weights = parseDoubleArray(p_weights_string);
            p_weights = normalizeVector(p_weights);
        }
    }
    @Override
    public final void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {


        Evidence[] evidences = datastore.findLastEvidences(
                profile.getTriggerLabel(),
                event.getSubject());
        evidences_number = evidences.length;
        //Store elements un a TreeMap to have an order
        //(based on label alphabetic order)
        Map<String, String[]> agent_labels = new TreeMap<>();

        long last_time = 0;
        double[] scores = new double[evidences_number];
        for (int i = 0; i < evidences_number; i++) {
            agent_labels.put(evidences[i].getLabel(),
                    new String[]{Double.toString(evidences[i].getScore()),
                            evidences[i].getId()});
            if (last_time < evidences[i].getTime()) {
                last_time = evidences[i].getTime();
            }
        }
        this.initParams(profile);
        WOWA aggregator = new WOWA(w_weights, p_weights);
        Evidence ev = new Evidence();
        String agents_output = "";
        int i = 0;
        for (String key : agent_labels.keySet()) {
            agents_output = agents_output + "<br />Agent("
                    + key + ") : Score(" + agent_labels.get(key)[0]
                    + ") : Id(" + agent_labels.get(key)[1] + ")";
            ev.references().add(agent_labels.get(key)[1]);
            //Set score elements in the double[]
            // following the alphabetic order (TreeMap)
            scores[i] = Double.parseDouble(agent_labels.get(key)[0]);
            i++;
        }
        ev.setSubject(event.getSubject());
        ev.setScore(aggregator.aggregate(scores));
        ev.setTime(last_time);
        ev.setReport("WOWA Aggregation generated for evidences with"
                + " label " + profile.getTriggerLabel()
                + "<br /> Agents used for the aggregation: "
                + agents_output);
        datastore.addEvidence(ev);
    }

    /**
     * Method to generate default W weights vector.
     * Values are 1 / evidences number.
     * @return
     */
    private double[] generateDefaultWVector() {
        double[] vector = new double[evidences_number];
        for (int i = 0; i < evidences_number; i++) {
            vector[i] = 1.0 / evidences_number;
        }
        return vector;
    }

    /**
     * Method to generate default P weights vector.
     * Default values are {0.2, 0.4, 0.3, 0.1, 0.0....}
     * NEED TO ADD BEHAVIOR IF NUMBER OF EVIDENCE IS SMALLER THAN 4 !!!
     * @return
     */
    private double[] generateDefaultPVector() {
        double[] new_weights = new double[evidences_number];
        if (evidences_number > DEFAULT_P_WEIGHTS.length) {
            System.arraycopy(DEFAULT_P_WEIGHTS,
                    0,
                    new_weights,
                    0,
                    evidences_number - 1);
        } else if (evidences_number < DEFAULT_P_WEIGHTS.length) {
            System.arraycopy(DEFAULT_P_WEIGHTS,
                    0,
                    new_weights,
                    0,
                    new_weights.length);
        }

        return new_weights;
    }

    /**
     * Method to check if the String parameter from yml
     * file provides correct parameters.
     * If parameters are correct ( String != null) and
     * number of element == number of evidences
     * Return False
     * If incorrect parameters, return true
     * @param parameters_string
     * @return
     */
    final boolean checkDefaultParameters(final String parameters_string) {
        if (parameters_string == null) {
            return true;
        } else {
            return parameters_string.split(",").length != evidences_number;
        }

    }

    /**
     * Method to parse a String[] to a double[].
     * @param parameters
     * @return
     */
    final double[] parseDoubleArray(final String parameters) {
        String[] split_weights = parameters.split(",");
        double[] weights = new double[split_weights.length];
        for (int i = 0; i < evidences_number; i++) {
            weights[i] = Double.parseDouble(split_weights[i]);
        }
        return weights;
    }

    /**
     * Method to normalize vector.
     * Avoid error if the sum of weights are != 1.0 (mandotory for WOWA).
     * @param vector
     * @return
     */
    static double[] normalizeVector(final double[] vector) {
        double sum = 0.0;
        double threshold = 0.0000000001;
        for (double el : vector) {
            sum += el;
        }
        if (sum <= threshold) {
            throw new IllegalArgumentException(
                    "Sum of weights in vector must be different of 0"
            );
        }
        if (Math.abs(sum - 1.0) <= threshold) {
            return vector;
        }
        double[] normalized_vector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized_vector[i] = vector[i] / sum;
        }
        return normalized_vector;
    }


}
