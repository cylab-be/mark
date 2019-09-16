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

package be.cylab.mark.example;

import be.cylab.mark.core.Subject;

/**
 *
 * @author Thibault Debatty
 */
public class ExampleSubject implements Subject {

    private String name;

    /**
     * Undefined link.
     */
    public ExampleSubject() {
    }

    /**
     *
     * @param client
     */
    public ExampleSubject(final String client) {
        this.name = client;
    }

    @Override
    public final String toString() {
        return name;
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 59 * hash;
        if (this.name != null) {
            hash += this.name.hashCode();
        }
        hash = 59 * hash;

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

        ExampleSubject other = (ExampleSubject) obj;

        return this.name.equals(other.name);

    }

    /**
     *
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public final void setName(final String name) {
        this.name = name;
    }
}
