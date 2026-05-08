package data_management;

import com.alerts.strategies.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlertStrategyTest {

    @Test
    void testBloodPressureStrategyHighSystolic() {
        Patient p = new Patient(1);
        p.addRecord(185.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertTrue(new BloodPressureStrategy().checkAlert(p, records));
    }

    @Test
    void testBloodPressureStrategyNormal() {
        Patient p = new Patient(2);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(80.0, "DiastolicPressure", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertFalse(new BloodPressureStrategy().checkAlert(p, records));
    }

    @Test
    void testHeartRateStrategyAbnormal() {
        Patient p = new Patient(3);
        p.addRecord(40.0, "HeartRate", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertTrue(new HeartRateStrategy().checkAlert(p, records));
    }

    @Test
    void testHeartRateStrategyNormal() {
        Patient p = new Patient(4);
        p.addRecord(72.0, "HeartRate", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertFalse(new HeartRateStrategy().checkAlert(p, records));
    }

    @Test
    void testOxygenSaturationStrategyLow() {
        Patient p = new Patient(5);
        p.addRecord(88.0, "Saturation", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertTrue(new OxygenSaturationStrategy().checkAlert(p, records));
    }

    @Test
    void testOxygenSaturationStrategyNormal() {
        Patient p = new Patient(6);
        p.addRecord(97.0, "Saturation", 1000L);
        List<PatientRecord> records = p.getRecords(0, Long.MAX_VALUE);
        assertFalse(new OxygenSaturationStrategy().checkAlert(p, records));
    }
}
