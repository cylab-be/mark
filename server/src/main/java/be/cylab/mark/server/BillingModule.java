/*
 * The MIT License
 *
 * Copyright 2019 bunyamin.
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
package be.cylab.mark.server;

import com.google.inject.AbstractModule;
import java.io.File;
import java.io.FileNotFoundException;
import be.cylab.mark.activation.ActivationController;
import be.cylab.mark.activation.ActivationControllerInterface;
import be.cylab.mark.activation.ExecutorInterface;
import be.cylab.mark.activation.IgniteExecutor;
import be.cylab.mark.activation.ThreadsExecutor;
import org.slf4j.LoggerFactory;

/**
 * Used for dependency injection, and avoid the utilization of "new".
 *
 * @author Bunyamin Aslan
 */
public class BillingModule extends AbstractModule {

    private final File config_file;
    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(BillingModule.class);

    /**
     * Constructor of BillingModule.
     *
     * @param config_file used to instantiate Config.
     */
    public BillingModule(final File config_file) {
        this.config_file = config_file;
    }

    @Override
    protected final void configure() {
        // read config file
        Config config = null;
        try {
            config = Config.fromFile(config_file);

        } catch (FileNotFoundException ex) {
            LOGGER.error("File not found: " + config_file);
            return;
        } catch (Exception ex) {
            LOGGER.error("Invalid configuration: " + ex.getMessage());
            return;
        }
        bind(Config.class).toInstance(config);

        //Associate Interface to class
        bind(ActivationControllerInterface.class).to(
                ActivationController.class);

        if (config.getExecutorClass().equals(IgniteExecutor.class.getName())) {
            bind(ExecutorInterface.class).to(IgniteExecutor.class);
        } else {
            bind(ExecutorInterface.class).to(ThreadsExecutor.class);
        }

    }

}
