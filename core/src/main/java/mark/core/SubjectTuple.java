/*
 * The MIT License
 *
 * Copyright 2019 georgi.
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
package mark.core;

/**
 * The SubjectTuple class represents a tuple of Subject-Timestamp.
 * 
 * It represents the data for a given Subject and the timestamp that the 
 * data was submitted to the database.
 * It's use is to hold the time information that can be important
 * for the detection agents.
 * @author Georgi Nikolov
 * @param <T>
 */
public class SubjectTuple<T extends Subject> {

    private T subject;
    private Long timestamp;

    /**
     * Undefined SubjectTuple.
     */
    public SubjectTuple() {        
    }

    /**
     *
     * @param subject
     * @param timestamp
     */
    public SubjectTuple(T subject, long timestamp) {
        this.subject = subject;
        this.timestamp = timestamp;
    }

    /**
     * Get the Subject.
     * @return
     */
    public final T getSubject() {
        return this.subject;
    }

    /**
     * Get the timestamp.
     * @return
     */
    public final long getTimestamp() {
        return this.timestamp;
    }

    /**
     *
     * @return
     */
    @Override
    public final int hashCode() {
//        int hash = this.subject.hashCode();
//
//        hash = 59 * hash;
//
//        hash += this.timestamp.hashCode();
//
//        return hash;
        return this.subject.hashCode();
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        SubjectTuple other = (SubjectTuple) obj;

        return this.subject.equals(other.subject);
    }
    
}
