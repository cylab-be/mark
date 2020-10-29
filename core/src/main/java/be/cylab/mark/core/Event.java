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
package be.cylab.mark.core;

/**
 * Represents an event of data (or evidence) inserted in the database. Contains
 * the label of inserted data, subject and timestamp. Used to trigger detectors
 * that have to be scheduled.
 *
 * @author tibo
 * @param <T> the actual subject class
 */
public class Event<T extends Subject> {
    private final String label;
    private final T subject;
    private final long timestamp;
    private final String id;

    /**
     *
     * @param label
     * @param subject
     * @param timestamp
     * @param id of the data or evidence that caused this event
     */
    public Event(
            final String label, final T subject, final long timestamp,
            final String id) {
        this.label = label;
        this.subject = subject;
        this.timestamp = timestamp;
        this.id = id;
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
     * @return
     */
    public final T getSubject() {
        return subject;
    }

    /**
     *
     * @return
     */
    public final long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the id of the data or evidence that caused this event.
     * @return
     */
    public final String getId() {
        return id;
    }


}
