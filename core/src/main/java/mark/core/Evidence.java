package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class Evidence<T extends Subject> {

    public String id = "";
    public String label = "";
    /**
     * The time variable is the timestamp attributed to the Evidence and is in 
     * format: number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public long time;
    public T subject;
    public double score;
    public String report = "";

    @Override
    public String toString() {
        return label + " : " + report;
    }

}
