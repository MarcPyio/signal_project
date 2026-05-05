package data_management;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DataStorageSingletonTest {

    @Test
    public void testOnlyOneInstanceExists() {
        //Attempt to grab the Singleton instance twice
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();

        // Prove that they are not just identical, but the EXACT same object
        assertSame(instance1, instance2, "Both variables should point to the exact same Singleton instance in memory.");
    }

    @Test
    public void testSharedState() {

        // (Optional but highly recommended)
        // Prove that modifying one instance modifies "both" (since they are the same)
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();

        //add a patient to instance1
        instance1.addPatientData(999, 100.0, "WhiteBloodCells", 1714376789050L);

        // Now check if instance2 can see that patient (by retrieving the patientId and comparing it to the created PatientId)
        assertEquals(999, instance2.getAllPatients().getLast().getPatientId(), "State should be shared across the Singleton.");
    }
}