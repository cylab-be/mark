package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class RawData<T extends Subject> {
    public String label = "";
    /**
     * The time variable is the timestamp attributed to the RawData and is in 
     * format: number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public long time;
    public T subject;
    public String data = "";

}
