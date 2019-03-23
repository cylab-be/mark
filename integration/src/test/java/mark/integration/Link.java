/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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

package mark.integration;

import mark.core.Subject;

/**
 *
 * @author Thibault Debatty
 */
public class Link implements Subject {

    private String client;
    private String server;

    /**
     * Undefined link.
     */
    public Link() {
    }

    /**
     *
     * @param client
     * @param server
     */
    public Link(final String client, final String server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public final String toString() {
        return client + " : " + server;
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 59 * hash;
        if (this.client != null) {
            hash += this.client.hashCode();
        }
        hash = 59 * hash;

        if (this.server != null) {
            hash += this.server.hashCode();
        }
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Link other = (Link) obj;

        return this.client.equals(other.client)
                && this.server.equals(other.server);

    }

    /**
     * Get client IP.
     * @return
     */
    public final String getClient() {
        return client;
    }

    /**
     * Get server name.
     * @return
     */
    public final String getServer() {
        return server;
    }
}
