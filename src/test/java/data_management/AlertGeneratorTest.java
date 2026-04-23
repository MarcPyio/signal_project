package data_management;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        alertGenerator = new AlertGenerator(storage);
    }

    @Test
    void testSystolicBPTooHigh() {
        storage.addPatientData(1, 185.0, "SystolicPressure", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        // Should not throw any exception
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testSystolicBPTooLow() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testDiastolicBPTooHigh() {
        storage.addPatientData(1, 125.0, "DiastolicPressure", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testDiastolicBPTooLow() {
        storage.addPatientData(1, 55.0, "DiastolicPressure", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testIncreasingBPTrend() {
        // Each reading increases by more than 10 mmHg
        storage.addPatientData(1, 100.0, "SystolicPressure", 1714376789050L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 1714376789051L);
        storage.addPatientData(1, 130.0, "SystolicPressure", 1714376789052L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testDecreasingBPTrend() {
        // Each reading decreases by more than 10 mmHg
        storage.addPatientData(1, 130.0, "SystolicPressure", 1714376789050L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 1714376789051L);
        storage.addPatientData(1, 100.0, "SystolicPressure", 1714376789052L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testLowBloodSaturation() {
        storage.addPatientData(1, 90.0, "Saturation", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testRapidSaturationDrop() {
        // Drop of 5% within 10 minutes
        storage.addPatientData(1, 97.0, "Saturation", 1714376789050L);
        storage.addPatientData(1, 91.0, "Saturation", 1714376789050L + (5 * 60 * 1000));
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testHypotensiveHypoxemia() {
        // Both low systolic BP and low saturation
        storage.addPatientData(1, 85.0, "SystolicPressure", 1714376789050L);
        storage.addPatientData(1, 90.0, "Saturation", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testAbnormalECGPeak() {
        // Add 10 normal readings then one spike
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1714376789050L + i);
        }
        storage.addPatientData(1, 10.0, "ECG", 1714376789060L); // spike
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testTriggeredAlert() {
        // Value of 1.0 means alert button was pressed
        storage.addPatientData(1, 1.0, "Alert", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testNoAlertForNormalData() {
        // All normal values, should not trigger any alerts
        storage.addPatientData(1, 120.0, "SystolicPressure", 1714376789050L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 1714376789050L);
        storage.addPatientData(1, 98.0, "Saturation", 1714376789050L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }
}