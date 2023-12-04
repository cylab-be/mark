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
package be.cylab.mark.data;

import be.cylab.mark.core.DataAgentInterface;
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.ServerInterface;
import org.slf4j.LoggerFactory;

/**
 * Wrap an data source implementation to make it runnable by a classical
 * thread.
 * @author tibo
 */
public class SourceWrapper implements Runnable {

    private final DataAgentInterface source;
    private final DataAgentProfile profile;
    private final ServerInterface client;

    /**
     * Wrap an data source implementation to make it runnable by a classical
     * thread.
     * @param source
     * @param profile
     * @param client
     */
    public SourceWrapper(
            final DataAgentInterface source,
            final DataAgentProfile profile,
            final ServerInterface client) {
        this.source = source;
        this.profile = profile;
        this.client = client;
    }

    @Override
    public final void run() {
        try {
            this.source.run(profile, client);
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            LoggerFactory.getLogger(SourceWrapper.class).error(
                    "Failed to start data source: " + ex.getMessage());
        }
    }

}
