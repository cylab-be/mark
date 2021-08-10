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
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 */
public final class ClientWrapper implements ServerInterface {

    private final DetectionAgentProfile profile;
    private final Client client;
    private final JsonRequestListener request_listener;
    private final ArrayList<String> requests = new ArrayList<>();

    /**
     *
     * @param server_url
     * @param profile
     */
    public ClientWrapper(
            final URL server_url,
            final DetectionAgentProfile profile) {

        this.client = new Client(server_url);
        this.profile = profile;
        this.request_listener = new JsonRequestListener();
        this.client.getJsonRpcClient().setRequestListener(request_listener);
    }


    /**
     *
     * @param evidence
     * @throws Throwable
     */
    @Override
    public void addEvidence(final Evidence evidence) throws Throwable {
        evidence.setProfile(profile);
        evidence.setLabel(profile.getLabel());
        evidence.setRequests(requests);
        client.addEvidence(evidence);
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public String test() throws Throwable {
        return client.test();
    }

    /**
     *
     * @param data
     * @throws Throwable
     */
    @Override
    public void testString(final String data) throws Throwable {
        client.testString(data);
    }

    /**
     *
     * @param filename
     * @param time
     * @return
     * @throws Throwable
     */
    @Override
    public String createSharedFile(final String filename, final long time)
            throws Throwable {
        return client.createSharedFile(filename, time);
    }

    /**
     *
     * @param data
     * @throws Throwable
     */
    @Override
    public void addRawData(final RawData data) throws Throwable {
        client.addRawData(data);
    }

    /**
     *
     * @param bytes
     * @param filename
     * @return
     * @throws Throwable
     */
    @Override
    public ObjectId addFile(final byte[] bytes, final String filename)
            throws Throwable {
        return client.addFile(bytes, filename);
    }

    /**
     *
     * @param file_id
     * @return
     * @throws Throwable
     */
    @Override
    public byte[] findFile(final ObjectId file_id) throws Throwable {
        return client.findFile(file_id);
    }

    /**
     *
     * @param type
     * @param subject
     * @param from
     * @param till
     * @return
     * @throws Throwable
     */
    @Override
    public RawData[] findRawData(
            final String type, final Map<String, String> subject,
            final long from,
            final long till) throws Throwable {
        RawData[] data = client.findRawData(type, subject, from, till);
        requests.add(request_listener.getLastRequest());
        return data;
    }

    /**
     *
     * @param label
     * @param subject
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findEvidence(final String label,
            final Map<String, String> subject)
            throws Throwable {
        return client.findEvidence(label, subject);
    }

    /**
     *
     * @param label
     * @param subject
     * @param time
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findEvidenceSince(
            final String label, final Map<String, String> subject,
            final long time)
            throws Throwable {
        return client.findEvidenceSince(label, subject, time);
    }

    /**
     *
     * @param label
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findEvidence(final String label) throws Throwable {
        return client.findEvidence(label);
    }

    /**
     *
     * @param label
     * @param page
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findEvidence(
            final String label, final int page) throws Throwable {
        return client.findEvidence(label, page);
    }

    /**
     *
     * @param id
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence findEvidenceById(final String id) throws Throwable {
        return client.findEvidenceById(id);
    }

    /**
     *
     * @return
     */
    @Override
    public URL getURL() {
        return client.getURL();
    }

    /**
     *
     * @param label
     * @param subject
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findLastEvidences(
            final String label, final Map<String, String> subject)
            throws Throwable {
        return client.findLastEvidences(label, subject);
    }

    /**
     *
     * @param key
     * @return
     * @throws Throwable
     */
    @Override
    public Object getFromCache(final String key) throws Throwable {
        return client.getFromCache(key);
    }

    /**
     *
     * @param key
     * @param value
     * @throws Throwable
     */
    @Override
    public void storeInCache(final String key, final Object value)
            throws Throwable {
        client.storeInCache(key, value);
    }

    /**
     *
     * @param key
     * @param new_value
     * @param old_value
     * @return
     * @throws Throwable
     */
    @Override
    public boolean compareAndSwapInCache(
            final String key, final Object new_value, final Object old_value)
            throws Throwable {
        return client.compareAndSwapInCache(key, new_value, old_value);
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public DetectionAgentProfile[] activation() throws Throwable {
        return client.activation();
    }

    /**
     *
     * @throws Throwable
     */
    @Override
    public void pause() throws Throwable {
        client.pause();
    }

    /**
     *
     * @throws Throwable
     */
    @Override
    public void resume() throws Throwable {
        client.resume();
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Map status() throws Throwable {
        return client.status();
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public List history() throws Throwable {
        return client.history();
    }

    /**
     *
     * @throws Throwable
     */
    @Override
    public void reload() throws Throwable {
        client.reload();
    }

    /**
     *
     * @param profile
     * @throws Throwable
     */
    @Override
    public void setAgentProfile(final DetectionAgentProfile profile)
            throws Throwable {

        client.setAgentProfile(profile);
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public RawData[] findLastRawData() throws Throwable {

        return client.findLastRawData();
    }


    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Evidence[] findLastEvidences() throws Throwable {

        return client.findLastEvidences();
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @Override
    public DataAgentProfile[] sources() throws Throwable {

        return client.sources();
    }
}
