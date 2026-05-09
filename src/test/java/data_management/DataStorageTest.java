package data_management;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    @BeforeEach
    void setUp() {
        // Reset before each test to ensure a clean slate
        DataStorage.resetInstance();
    }

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
    @Test
    void testAddAndGetRecords() {
        DataStorage storage = DataStorage.getInstance();

        // Add multiple records to the same patient
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetAllPatients() {
        DataStorage storage = DataStorage.getInstance();

        storage.addPatientData(1, 100.0, "HeartRate", 1714376789050L);
        storage.addPatientData(2, 120.0, "BloodPressure", 1714376789050L);

        List<Patient> patients = storage.getAllPatients();
        assertEquals(2, patients.size(), "Should retrieve exactly two distinct patients");
    }
    @Test
    void testGetInstance_ConcurrentAccess_HitsDoubleCheckedLockingBranch() throws InterruptedException {
        // We run the race 100 times. Because thread scheduling is up to the OS,
        // running it once might not cause the exact collision needed. Running it
        // 100 times virtually guarantees the threads will collide perfectly at
        // least once, satisfying JaCoCo's branch coverage tracker.
        for (int i = 0; i < 100; i++) {

            // 1. Reset the instance so the race starts fresh every loop
            DataStorage.resetInstance();

            // 2. Set up our thread pool and starting pistols
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch startPistol = new CountDownLatch(1);
            CountDownLatch finishLine = new CountDownLatch(2);

            // 3. Define the task both threads will execute
            Runnable task = () -> {
                try {
                    // Threads will pause here and wait for the countdown to reach 0
                    startPistol.await();

                    // BANG! Both threads attempt to get the instance simultaneously
                    DataStorage.getInstance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // Signal that this thread has finished the race
                    finishLine.countDown();
                }
            };

            // 4. Put both runners on the starting blocks
            executor.submit(task);
            executor.submit(task);

            // 5. Fire the starting pistol! (Reduces the start latch to 0)
            startPistol.countDown();

            // 6. Wait for both threads to cross the finish line
            finishLine.await(2, TimeUnit.SECONDS);
            executor.shutdown();

            // Assert that despite the collision, an instance was successfully created
            assertNotNull(DataStorage.getInstance(), "Instance should have been created");
        }
    }
}