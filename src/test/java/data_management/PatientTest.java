package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void testAddAndGetRecordsInRange_IncludesBoundaries() {
        Patient patient = new Patient(1);

        // Add records exactly ON the boundary to test >= and <=
        patient.addRecord(100.0, "HeartRate", 1000L); // Exactly on startTime
        patient.addRecord(200.0, "HeartRate", 1500L); // Safely inside range
        patient.addRecord(300.0, "HeartRate", 2000L); // Exactly on endTime

        // Query the exact range: 1000 to 2000
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);

        // Assert: All three should be caught because the operator is inclusive (>= and <=)
        assertEquals(3, records.size(), "Should include records exactly on the time boundaries");
    }

    //Tests the False branch where record.getTimestamp() < startTime
    @Test
    void testGetRecords_TooEarly_HitsFalseBranch() {
        Patient patient = new Patient(1);

        // Record is older than the start time
        patient.addRecord(100.0, "HeartRate", 500L);

        // Query range: 1000 to 2000
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);

        assertEquals(0, records.size(), "Should ignore records occurring before the start time");
    }

    // Tests the False branch where record.getTimestamp() > endTime
    @Test
    void testGetRecords_TooLate_HitsFalseBranch() {
        Patient patient = new Patient(1);

        // Record is newer than the end time
        patient.addRecord(100.0, "HeartRate", 2500L);

        // Query range: 1000 to 2000
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);

        assertEquals(0, records.size(), "Should ignore records occurring after the end time");
    }

    @Test
    void testGetPatientId() {
        Patient patient = new Patient(42);
        assertEquals(42, patient.getPatientId());
    }

    @Test
    void testEmptyRecords() {
        Patient patient = new Patient(1);
        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testAddDuplicateRecord_IsIgnored() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1714376789050L);

        // Attempt to add the exact same record type at the exact same timestamp
        patient.addRecord(150.0, "HeartRate", 1714376789050L);

        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertEquals(1, records.size(), "Duplicate record should not be added");
        assertEquals(100.0, records.get(0).getMeasurementValue(), "Original value should remain");
    }
}