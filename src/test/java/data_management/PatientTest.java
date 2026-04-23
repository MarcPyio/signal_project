package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void testAddAndGetRecordsInRange() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1714376789050L);
        patient.addRecord(200.0, "HeartRate", 1714376789051L);
        patient.addRecord(300.0, "HeartRate", 1714376789052L);

        List<PatientRecord> records = patient.getRecords(1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsOutOfRange() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1714376789050L);

        // Query a range that doesn't include the record
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(0, records.size());
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
}