package be.cylab.mark.core;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class RawData<T extends Subject> {

    private String label = "";
    /**
     * The time variable is the timestamp attributed to the RawData and is in
     * format: number of milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    private long time;
    private T subject;
    private String data = "";

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

}
