package mark.activation;

import java.util.Map;
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
    void setParameters(Map<String, String> parameters);
    void setLabel(String label);
    void setInputLabel(String input_label);
    void setSubject(Subject subject);
    void setDatastoreUrl(String datastore_url);
    void setSubjectAdapter(SubjectAdapter subject_adapter);
}
