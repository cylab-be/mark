package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class Evidence {
    public String agent;
    public int time;
    public String client;
    public String server;
    public double score;
    public String report;

    @Override
    public String toString() {
        return agent + " : " + report;
    }

}
