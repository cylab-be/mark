package mark.activation;

/**
 *
 * @author Thibault Debatty
 */
public class ActivationProfile {

    /**
     * Eg: RAW_DATA.
     */
    public ActivationController.Collection collection;

    /**
     * Eg: http.
     */
    public String type;

    public int condition_count;

    public int condition_time = Integer.MAX_VALUE;

    /**
     *
     */
    public String class_name;

}
