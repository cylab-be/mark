package be.cylab.mark.core;

import be.cylab.mark.core.DetectionAgentProfile;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class DetectionAgentProfileTest
    extends TestCase
{


    /**
     * Rigourous Test :-)
     */
    public void testParse()
    {
        InputStream stream = getClass()
                .getResourceAsStream("/detection.dummy.yml");

        DetectionAgentProfile profile =
                DetectionAgentProfile.fromInputStream(stream);

        assertEquals("mark.core.DummyDetector", profile.getClassName());
        assertEquals("detection.dummy", profile.getLabel());
        assertEquals("data.http", profile.getTriggerLabel());

    }
}
