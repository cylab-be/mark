package mark.detection;

import java.net.URL;
import mark.activation.DetectionAgentProfile;
import mark.core.Subject;
import mark.core.SubjectAdapter;

/**
 * The minimum interface for implementing a detection agent, so it can be
 * instantiated and run by the server.
 * The most simple way to create a detection agent is to extend
 * AbstractDetectionAgent, which provides a lot of helpers.
 * @author Thibault Debatty
 */
public interface DetectionAgentInterface extends Runnable {
    /**
     * Set the profile for this detection agent (contains stuff like label,
     * trigger labels, parameters etc).
     * The setters are used by the activation controller to configure the
     * detection task.
     * @param profile
     */
    void setDetectionAgentProfile(DetectionAgentProfile profile);

    /**
     * Set the label that actually triggered this detector (as the profile may
     * specify a pattern).
     * The setters are used by the activation controller to configure the
     * detection task.
     * @param actual_trigger_label
     */
    void setActualTriggerLabel(String actual_trigger_label);

    /**
     * Set the subject that this detector must analyze.
     * The setters are used by the activation controller to configure the
     * detection task.
     * @param subject
     */
    void setSubject(Subject subject);

    /**
     * Set the URL of datastore.
     * The setters are used by the activation controller to configure the
     * detection task.
     *
     * @param datastore_url
     */
    void setDatastoreUrl(URL datastore_url);

    /**
     * Set the subject adapter.
     * The setters are used by the activation controller to configure the
     * detection task.
     * @param subject_adapter
     */
    void setSubjectAdapter(SubjectAdapter subject_adapter);
}
