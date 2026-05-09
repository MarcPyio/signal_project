package alerts;

import com.alerts.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ECGAlertFactoryTest {

    @Test
    public void testCreateECGAlert() {

        AlertFactory factory = new ECGAlertFactory();
        String expectedPatientId = "Patient-123";
        String expectedCondition = "Systolic BP Too High";
        long expectedTimestamp = 1683050000000L; // Arbitrary timestamp for testing

        Alert generatedAlert = factory.createAlert(expectedPatientId, expectedCondition, expectedTimestamp);


        // Ensure the factory actually returned an object
        assertNotNull(generatedAlert, "The factory should not return null.");

        // Check Polymorphism: Ensure it created the specific subclass, not just a generic Alert
        assertTrue(generatedAlert instanceof ECGAlert, "The generated alert should be an instance of ECGAlert.");

        // Verify the data inside the object matches exactly what we passed in
        assertEquals(expectedPatientId, generatedAlert.getPatientId(), "The Patient ID did not match.");
        assertEquals(expectedCondition, generatedAlert.getCondition(), "The Condition did not match.");
        assertEquals(expectedTimestamp, generatedAlert.getTimestamp(), "The Timestamp did not match.");
    }
}