package be.cylab.mark.core;

import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Evidence<T extends Subject>
        implements Comparable<Evidence>, Serializable {

    private String id = "";
    private String label = "";

    /**
     * The time variable is the timestamp attributed to the Evidence and is in
     * format: number of milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    private long time;
    private T subject;
    private double score;
    private String report = "";

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
     *
     * @return
     */
    public final long getTime() {
        return time;
    }

    /**
     *
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
