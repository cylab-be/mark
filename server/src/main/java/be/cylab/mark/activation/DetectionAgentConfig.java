/*
 * The MIT License
 *
 * Copyright 2021 tibo.
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
package be.cylab.mark.activation;

import be.cylab.mark.server.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.LoggerFactory;

/**
 * Config parameters that are transmitted to detection agents.
 * A subset of global configuration.
 * @author tibo
 */
public final class DetectionAgentConfig {

    /**
     * Create a subset of the config.
     * @param config
     * @return
     * @throws MalformedURLException if serer URL is not correct
     */
    public static DetectionAgentConfig fromConfig(final Config config)
            throws MalformedURLException {

        DetectionAgentConfig c = new DetectionAgentConfig();
        c.server_url = config.getDatastoreUrl();


        try {
            c.data_directory = config.getDataDirectory();
        } catch (FileNotFoundException ex) {
            LoggerFactory.getLogger(ActivationController.class).warn(
                    "Invalid data directory: " + ex.getMessage() + ". "
                    + "Any detector trying to save to shared data will crash!");
        }

        return c;
    }

    private URL server_url;
    private File data_directory;

    /**
     * Get the URL of the datastore server.
     * @return
     */
    public URL getServerUrl() {
        return server_url;
    }

    /**
     * Get the directory where shared data should be written.
     * @return
     */
    public File getDataDirectory() {
        return data_directory;
    }
}
