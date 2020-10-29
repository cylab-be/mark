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

package be.cylab.mark.datastore;

import be.cylab.mark.activation.ActivationControllerInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import java.util.List;
import java.util.Map;
import org.apache.ignite.cluster.ClusterMetrics;

/**
 *
 * @author Thibault Debatty
 */
public class DummyActivationContoller implements ActivationControllerInterface {

    @Override
    public void notifyEvidence(Evidence evidence) {

    }

    @Override
    public void notifyRawData(RawData data) {

    }

    public ClusterMetrics getIgniteMetrics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<DetectionAgentProfile> getProfiles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map getExecutorStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pauseExecution() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resumeExecution() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAgentProfile(DetectionAgentProfile profile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
