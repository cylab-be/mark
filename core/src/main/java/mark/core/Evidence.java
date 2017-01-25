package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class Evidence<T extends Subject> {

    public String id;
    public String label;
    public int time;
    public T subject;
    public double score;
    public String report;

    @Override
    public String toString() {
        return label + " : " + report;
    }

}
