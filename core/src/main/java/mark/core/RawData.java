package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class RawData<T extends Subject> {
    public String label = "";
    public long time;
    public T subject;
    public String data = "";

}
