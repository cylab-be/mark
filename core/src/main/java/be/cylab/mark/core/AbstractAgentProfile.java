/*
 * The MIT License
 *
 * Copyright 2020 tibo.
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
package be.cylab.mark.core;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tibo
 */
public class AbstractAgentProfile {

    /**
     * Name of the class to instantiate.
     */
    private String class_name;

    /**
     * The label for the data that will be added to the datastore.
     */
    private String label;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     *
     * @return
     */
    public final String getClassName() {
        return class_name;
    }

    /**
     *
     * @param class_name
     */
    public final void setClassName(final String class_name) {
        this.class_name = class_name;
    }

    /**
     *
     * @return
     */
    public final String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     */
    public final void setLabel(final String label) {
        this.label = label;
    }

    /**
     *
     * @return
     */
    public final Map<String, String> getParameters() {
        return parameters;
    }

    /**
     *
     * @param parameters
     */
    public final void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Set a single parameter.
     * @param key
     * @param value
     */
    public final void setParameter(final String key, final String value) {
        this.parameters.put(key, value);
    }

        /**
     *
     * @param name
     * @return
     */
    public final String getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * Get the value of this parameter, of return default_value if not set.
     * @param name
     * @param default_value
     * @return
     */
    public final String getParameterOrDefault(
            final String name, final String default_value) {
        return parameters.getOrDefault(name, default_value);
    }

    /**
     *
     * @param name
     * @param default_value
     * @return
     */
    public final int getParameterInt(
            final String name, final int default_value) {

        if (this.getParameter(name) == null) {
            return default_value;
        }

        try {
            return Integer.valueOf(this.getParameter(name));
        } catch (NumberFormatException ex) {
            return default_value;
        }
    }

    /**
     *
     * @param name
     * @param default_value
     * @return
     */
    public final double getParameterDouble(
            final String name, final double default_value) {

        if (this.getParameter(name) == null) {
            return default_value;
        }

        try {
            return Double.valueOf(this.getParameter(name));
        } catch (NumberFormatException ex) {
            return default_value;
        }
    }

}
