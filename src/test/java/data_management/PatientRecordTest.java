package data_management;

import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatientRecordTest {

    @Test
    void testPatientRecordGetters() {
        PatientRecord record = new PatientRecord(10, 120.5, "BloodPressure", 1714376789000L);

        assertEquals(10, record.getPatientId());
        assertEquals(120.5, record.getMeasurementValue());
        assertEquals("BloodPressure", record.getRecordType());
        assertEquals(1714376789000L, record.getTimestamp());
    }
}