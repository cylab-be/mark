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
package mark.server;

import com.google.inject.AbstractModule;
import java.io.File;
import java.io.FileNotFoundException;
import mark.activation.ActivationController;
import mark.activation.ActivationControllerInterface;

/**
 * Used for dependency injection, and avoid "new" in server code.
 *
 * @author Bunyamin Aslan
 */
public class BillingModule extends AbstractModule {

    private final File config_file;

    public BillingModule(File config_file) {
        this.config_file = config_file;
    }

    @Override
    protected void configure() {
        bind(ActivationControllerInterface.class).
                to(ActivationController.class);
        try {
            bind(Config.class).toInstance(new Config(config_file));
        } catch (FileNotFoundException ex) {
            //todo
        }
    }

}
