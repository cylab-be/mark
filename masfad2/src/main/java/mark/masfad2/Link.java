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

package mark.masfad2;

import mark.core.Subject;

/**
 *
 * @author Thibault Debatty
 */
public class Link implements Subject {

    public String client;
    public String server;

    public Link() {
    }

    public Link(String client, String server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public String toString() {
        return client + " : " + server;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.client != null ? this.client.hashCode() : 0);
        hash = 59 * hash + (this.server != null ? this.server.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Link other = (Link) obj;
        if ((this.client == null) ? (other.client != null) : !this.client.equals(other.client)) {
            return false;
        }
        if ((this.server == null) ? (other.server != null) : !this.server.equals(other.server)) {
            return false;
        }
        return true;
    }
}
