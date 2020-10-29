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
package be.cylab.mark.integration;

import be.cylab.mark.activation.ActivationController;
import be.cylab.mark.activation.ExecutorInterface;
import be.cylab.mark.activation.ThreadsExecutor;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.datastore.Datastore;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.Server;
import junit.framework.TestCase;

/**
 *
 * @author tibo
 */
public class MarkCase extends TestCase {

    private Server server;
    private ActivationController activation_controller;

    @Override
    protected final void setUp() throws Exception {

        Config config = Config.getTestConfig();
        config.setAdapterClass(LinkAdapter.class.getName());

        ExecutorInterface executor = new ThreadsExecutor();

        activation_controller
                = new ActivationController(config, executor);
        activation_controller.setAgentProfile(
                DetectionAgentProfile.fromInputStream(
                getClass().getResourceAsStream("/detection.dummy.yml")));

        try {
            server = new Server(
                    config,
                    activation_controller,
                    new Datastore(config, activation_controller));
            server.start();
        } catch (Throwable ex) {
            throw new Exception("failed to start server", ex);
        }

    }

    @Override
    protected final void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        super.tearDown();
    }



    /**
     * Set up normal server.
     *
     * @return server
     */
    protected final Server getTestServer() {
        return server;
    }

    protected final ActivationController getActivationController() {
        return activation_controller;
    }
}
