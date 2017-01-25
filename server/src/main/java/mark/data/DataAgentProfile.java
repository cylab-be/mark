package mark.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import mark.client.Client;
import mark.server.Config;
import mark.server.InvalidProfileException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DataAgentProfile {

    /**
     * Name of the class to instantiate (must implement DataAgentInterface).
     */
    public String class_name;

    public String label;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    public Map<String, String> parameters;

    public String path;

    private static final Yaml PARSER = new Yaml(new Constructor(DataAgentProfile.class));

    public final static DataAgentProfile fromFile(final File file) throws FileNotFoundException {
        DataAgentProfile profile = DataAgentProfile.fromInputStream(new FileInputStream(file));
        profile.path = file.getAbsolutePath();
        return profile;
    }

    public static final DataAgentProfile fromInputStream(final InputStream input) {
        return PARSER.loadAs(input, DataAgentProfile.class);
    }

    /**
     *
     * @param config
     * @return
     * @throws InvalidProfileException
     * @throws java.net.MalformedURLException
     */
    public AbstractDataAgent getInstance(Config config)
            throws InvalidProfileException, MalformedURLException {

        AbstractDataAgent agent;
        try {
            agent = (AbstractDataAgent) Class.forName(class_name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        } catch (InstantiationException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        }

        agent.setProfile(this);
        agent.setDatastore(new Client(
                config.getDatastoreUrl(),
                config.getSubjectAdapter()));

        return agent;
    }
}
