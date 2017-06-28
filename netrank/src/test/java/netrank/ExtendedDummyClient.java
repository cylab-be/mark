/*
 * The MIT License
 *
 * Copyright 2017 georgi.
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
package netrank;

import java.util.LinkedList;
import mark.activation.DummyClient;
import mark.core.Evidence;
import mark.core.Subject;

/**
 *
 * @author Georgi Nikolov
 * extended the DummyClient to support adding and fetching Evidence for general
 * tests if agents add the correct type of evidence to the database
 * @param <T>
 */
public class ExtendedDummyClient<T extends Subject> extends DummyClient<T>  {

    private final LinkedList<Evidence> evidence = new LinkedList<>();

        @Override
    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
        this.evidence.add(evidence);
    }

    @Override
    public Evidence[] findEvidence(String label) {
        return new Evidence[1];
    }

    public LinkedList<Evidence> getEvidences() throws Throwable {
        return this.evidence;
    }
    
}
