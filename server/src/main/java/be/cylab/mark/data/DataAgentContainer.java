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
package be.cylab.mark.data;

import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.DataAgentInterface;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import be.cylab.mark.client.Client;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.server.Config;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
public class DataAgentContainer extends Thread {

    private final Config config;
    private final DataAgentProfile profile;

    public DataAgentContainer(
            final DataAgentProfile profile, final Config config) {
        this.profile = profile;
        this.config = config;
    }

    @Override
    public final void run() {
        try {
            DataAgentInterface source = profile.createInstance();
            source.run(
                    profile,
                    new Client(
                            config.getDatastoreUrl(),
                            config.getSubjectAdapter()));

        } catch (Throwable ex) {
            LoggerFactory.getLogger(
                    DataAgentContainer.class.getName()).error(
                    "Data agent failed to run!", ex);
        }
    }

}
