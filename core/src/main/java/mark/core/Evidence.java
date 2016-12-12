package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class Evidence {
    public String label;
    public int time;
    public Link subject;
    public double score;
    public String report;

    @Override
    public String toString() {
        return label + " : " + report;
    }

}
