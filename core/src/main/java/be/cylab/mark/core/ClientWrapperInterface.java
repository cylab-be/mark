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
package be.cylab.mark.core;

import java.io.File;
import java.io.IOException;

/**
 * List all methods that will be accessible by detection agents.
 * @author tibo
 */
public interface ClientWrapperInterface extends ServerInterface {

    /**
     * Create a File in the data directory, which is actually a volume shared
     * between all containers.
     * @param filename
     * @return
     * @throws java.io.IOException if we cannot create the file
     */
    File createSharedFile(String filename) throws IOException;

    /**
     * Convert a shared file into a URL (from mark-web).
     *
     * Can be used by detector to produce an HTML report with correct links.
     *
     * Ex: /mark/data/some_picture.png => /data/some_picture.png
     *
     * @param shared_file
     * @return
     */
    String getURLFromFile(File shared_file);

}
