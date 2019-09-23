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
package be.cylab.mark.activation;

import be.cylab.mark.client.Client;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.Subject;
import be.cylab.mark.core.SubjectAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * A wrapper around the datastore client code that performs some additional
 * tasks before reading / writing data or evidences to the datastore.
 *
 * For example: link detection report to detection profile
 *
 * A ClientWrapper object is provided to the detection algorithms by the
 * DetectionAgentContainer.
 *
 * @author tibo
 * @param <T>
 */
public class ClientWrapper<T extends Subject> implements ServerInterface {

    private final DetectionAgentProfile profile;
    private final Client<Subject> client;
    private final JsonRequestListener request_listener;
    private final ArrayList<String> requests = new ArrayList<>();

    public ClientWrapper(
            final URL server_url,
            final SubjectAdapter adapter,
            final DetectionAgentProfile profile) {

        this.client = new Client<>(server_url, adapter);
        this.profile = profile;
        this.request_listener = new JsonRequestListener();
        this.client.getJsonRpcClient().setRequestListener(request_listener);
    }


    @Override
    public void addEvidence(final Evidence evidence) throws Throwable {
        evidence.setProfile(profile);
        evidence.setLabel(profile.getLabel());
        evidence.setRequests(requests);
        client.addEvidence(evidence);
    }

    @Override
    public String test() throws Throwable {
        return client.test();
    }

    @Override
    public void testString(String data) throws Throwable {
        client.testString(data);
    }

    @Override
    public void addRawData(RawData data) throws Throwable {
        client.addRawData(data);
    }

    @Override
    public ObjectId addFile(byte[] bytes, String filename) throws Throwable {
        return client.addFile(bytes, filename);
    }

    @Override
    public byte[] findFile(ObjectId file_id) throws Throwable {
        return client.findFile(file_id);
    }

    @Override
    public RawData[] findData(Document query) throws Throwable {
        RawData[] data = client.findData(query);
        requests.add(request_listener.getLastRequest());
        return data;
    }

    @Override
    public RawData[] findRawData(
            final String type, final Subject subject, final long from,
            final long till) throws Throwable {
        RawData[] data = client.findRawData(type, subject, from, till);
        requests.add(request_listener.getLastRequest());
        return data;
    }

    @Override
    public Evidence[] findEvidence(String label, Subject subject) throws Throwable {
        return client.findEvidence(label, subject);
    }

    @Override
    public Evidence[] findEvidenceSince(String label, Subject subject, long time) throws Throwable {
        return client.findEvidenceSince(label, subject, time);
    }

    @Override
    public Evidence[] findEvidence(String label) throws Throwable {
        return client.findEvidence(label);
    }

    @Override
    public Evidence[] findEvidence(String label, int page) throws Throwable {
        return client.findEvidence(label, page);
    }

    @Override
    public Evidence findEvidenceById(String id) throws Throwable {
        return client.findEvidenceById(id);
    }

    @Override
    public URL getURL() {
        return client.getURL();
    }

    @Override
    public Evidence[] findLastEvidences(String label, Subject subject) throws Throwable {
        return client.findLastEvidences(label, subject);
    }

    @Override
    public Object getFromCache(String key) throws Throwable {
        return client.getFromCache(key);
    }

    @Override
    public void storeInCache(String key, Object value) throws Throwable {
        client.storeInCache(key, value);
    }

    @Override
    public boolean compareAndSwapInCache(String key, Object new_value, Object old_value) throws Throwable {
        return client.compareAndSwapInCache(key, new_value, old_value);
    }

    @Override
    public Map executorStatus() throws Throwable {
        return client.executorStatus();
    }

    @Override
    public DetectionAgentProfile[] activation() throws Throwable {
        return client.activation();
    }

    @Override
    public void pause() throws Throwable {
        client.pause();
    }

    @Override
    public void resume() throws Throwable {
        client.resume();
    }

}
