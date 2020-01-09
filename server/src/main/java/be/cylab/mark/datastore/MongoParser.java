/*
 * The MIT License
 *
 * Copyright 2020 tibo.
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

import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.SubjectAdapter;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;

/**
 * Parses RawData and Evidence to Mongo documents.
 *
 * @author tibo
 */
public class MongoParser {

    public final String LABEL = "LABEL";
    public final String TIME = "TIME";
    public final String DATA = "DATA";
    public final String SCORE = "SCORE";
    public final String REPORT = "REPORT";
    public final String REFERENCES = "references";

    private final SubjectAdapter adapter;


    public MongoParser(final SubjectAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Convert from MongoDB document to RawData.
     *
     * @param doc
     * @return
     */
    public RawData convert(final Document doc) {

        RawData data = new RawData();
        data.setSubject(adapter.readFromMongo(doc));
        data.setData(doc.getString(DATA));
        data.setTime(doc.getLong(TIME));
        data.setLabel(doc.getString(LABEL));

        return data;
    }

    public Evidence convertEvidence(final Document doc) {

        Evidence evidence = new Evidence();
        evidence.setSubject(adapter.readFromMongo(doc));
        evidence.setScore(doc.getDouble(SCORE));
        evidence.setTime(doc.getLong(TIME));
        evidence.setLabel(doc.getString(LABEL));
        evidence.setReport(doc.getString(REPORT));
        evidence.setId(doc.getObjectId("_id").toString());
        evidence.setReferences(doc.getList(REFERENCES, String.class));
        evidence.setRequests(doc.getList("requests", String.class));

        Document profile_doc = doc.get("profile", Document.class);

        if (profile_doc != null) {
            DetectionAgentProfile profile = new DetectionAgentProfile();
            profile.setClassName(profile_doc.getString("class_name"));
            profile.setLabel(profile_doc.getString("label"));
            profile.setTriggerLabel(profile_doc.getString("trigger_label"));
            profile.setParameters(this.convertToMap(profile_doc.get("parameters", Document.class)));
            evidence.setProfile(profile);
        }

        return evidence;
    }

    public Document convert(Map map) {
        return new Document(map);
    }

    public HashMap convertToMap (Document doc) {
        HashMap map = new HashMap();

        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    /**
     * Convert from RawData to MongoDB document.
     *
     * @param data
     * @return
     */
    public Document convert(final RawData data) {

        Document doc = new Document()
                .append(LABEL, data.getLabel())
                .append(TIME, data.getTime())
                .append(DATA, data.getData());
        adapter.writeToMongo(data.getSubject(), doc);
        return doc;
    }

    /**
     * Convert from Evidence to MongoDB document.
     *
     * @param evidence
     * @return
     */
    public Document convert(final Evidence evidence) {

        Document doc = new Document()
                .append(LABEL, evidence.getLabel())
                .append(TIME, evidence.getTime())
                .append(SCORE, evidence.getScore())
                .append(TIME, evidence.getTime())
                .append(REPORT, evidence.getReport())
                .append(REFERENCES, evidence.getReferences())
                .append("requests", evidence.getRequests());

        if (evidence.getProfile() != null) {
            Document profile_doc = new Document()
                .append("class_name", evidence.getProfile().getClassName())
                .append("label", evidence.getProfile().getLabel())
                .append("trigger_label", evidence.getProfile().getTriggerLabel())
                .append("parameters", convert(evidence.getProfile().getParameters()));
            doc.append("profile", profile_doc);
        }

        adapter.writeToMongo(evidence.getSubject(), doc);
        return doc;
    }

}
