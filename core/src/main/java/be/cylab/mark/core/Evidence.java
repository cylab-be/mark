package be.cylab.mark.core;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Evidence<T extends Subject>
        implements Comparable<Evidence>, Serializable {

    private String id = "";
    private String label = "";
    private long time;
    private T subject;
    private double score;
    private String report = "";
    private List<String> references = new ArrayList<>();
    private List<String> requests = new ArrayList<>();
    private DetectionAgentProfile profile = new DetectionAgentProfile();


    @Override
    public final String toString() {
        return id + " : " + label;
    }

    /**
     *
     * @return
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public final void setId(final String id) {
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
     * @param label
     */
    public final void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Unix timestamp in milliseconds.
     *
     * @return
     */
    public final long getTime() {
        return time;
    }

    /**
     * Unix timestamp in milliseconds.
     * @param time
     */
    public final void setTime(final long time) {
        this.time = time;
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
     * @param subject
     */
    public final void setSubject(final T subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     */
    public final double getScore() {
        return score;
    }

    /**
     *
     * @param score
     */
    public final void setScore(final double score) {
        this.score = score;
    }

    /**
     *
     * @return
     */
    public final String getReport() {
        return report;
    }

    /**
     *
     * @param report
     */
    public final void setReport(final String report) {
        this.report = report;
    }

    /**
     *
     * @return
     */
    public final List<String> getReferences() {
        return references;
    }

    /**
     *
     * @param references
     */
    public final void setReferences(final List<String> references) {
        this.references = references;
    }

    /**
     * Get or set the ID of related Evidences.
     * @return
     */
    public final List<String> references() {
        return references;
    }

    /**
     * findData requests that have be executed by this detector.
     *
     * @return
     */
    public final List<String> getRequests() {
        return requests;
    }

    /**
     *
     * @param requests
     */
    public final void setRequests(final List<String> requests) {
        this.requests = requests;
    }


    /**
     * The DetectionAgentProfile of the detector that produced this reports.
     * Includes stuff like class name and configuration parameters of this
     * detector.
     * @return
     */
    public final DetectionAgentProfile getProfile() {
        return profile;
    }

    /**
     *
     * @param profile
     */
    public final void setProfile(final DetectionAgentProfile profile) {
        this.profile = profile;
    }


    @Override
    public final int compareTo(final Evidence other) {
        if (this.score >= other.score) {
            return 1;
        }

        return -1;
    }

    /**
     * ISO8601 representation of this event's timestamp.
     * @return
     */
    public final String timeFormatted() {
        return Instant.ofEpochMilli(this.time).toString();
    }
}
