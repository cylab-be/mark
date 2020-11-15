package be.cylab.mark.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Thibault Debatty
 */
public class RawData {

    private String label = "";
    /**
     * The time variable is the timestamp attributed to the RawData and is in
     * format: number of milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    private long time;
    private Map<String, String> subject = new HashMap<>();
    private String data = "";
    private String id = "";

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
    public final Map<String, String> getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     */
    public final void setSubject(final Map<String, String> subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     */
    public final String getData() {
        return data;
    }

    /**
     *
     * @param data
     */
    public final void setData(final String data) {
        this.data = data;
    }

    /**
     * ISO8601 representation of this event's timestamp.
     * @return
     */
    public final String timeFormatted() {
        return Instant.ofEpochMilli(this.time).toString();
    }

}
