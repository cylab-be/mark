/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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
package be.cylab.mark.activation;

import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Thibault Debatty
 */
public interface ActivationControllerInterface {

    /**
     *
     * @param evidence
     */
    void notifyEvidence(final Evidence evidence);

    /**
     * Trigger required tasks for this new RawData.
     *
     * @param data
     */
    void notifyRawData(final RawData data);

    /**
     *
     * @return
     */
    List<DetectionAgentProfile> getProfiles();

    /**
     *
     * @return
     */
    Map<String, Object> getExecutorStatus();

    /**
     * Pause execution.
     */
    void pauseExecution();

    /**
     * Resume execution.
     */
    void resumeExecution();

    /**
     * Check if running.
     * @return
     */
    boolean isRunning();

    /**
     * Reload configuration files for detection agent profiles.
     */
    void reload();

    /**
     * Add or update the configuration a detector. If profile.label is already
     * defined, the configuration is updated, otherwise a new detector is added.
     *
     * @param profile
     */
    void setAgentProfile(DetectionAgentProfile profile);
}
