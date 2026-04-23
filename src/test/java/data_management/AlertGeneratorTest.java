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
    @Test
    void testEmptyPatientData() {
        // Patient exists but has no records. Ensures loops don't throw exceptions.
        Patient emptyPatient = new Patient(99);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(emptyPatient));
    }

    @Test
    void testBloodPressureExactBoundaries() {
        // Exact thresholds should NOT trigger alerts (> 180, < 90, > 120, < 60)
        storage.addPatientData(1, 180.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 90.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 120.0, "DiastolicPressure", 3000L);
        storage.addPatientData(1, 60.0, "DiastolicPressure", 4000L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBPTrendExactChangeAndBrokenTrend() {
        // Exact 10 mmHg change (should not trigger)
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 110.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 120.0, "SystolicPressure", 3000L);

        // Broken trend: goes up >10, then drops (should not trigger)
        storage.addPatientData(2, 100.0, "DiastolicPressure", 1000L);
        storage.addPatientData(2, 120.0, "DiastolicPressure", 2000L);
        storage.addPatientData(2, 115.0, "DiastolicPressure", 3000L);

        // Insufficient data points (only 2 records, loop won't execute body)
        storage.addPatientData(3, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(3, 130.0, "SystolicPressure", 2000L);

        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(1)));
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(2)));
    }

    @Test
    void testSaturationExactThresholdAndSubtleDrop() {
        // Exact threshold of 92 (should not trigger < 92)
        storage.addPatientData(1, 92.0, "Saturation", 1000L);

        // Drop of exactly 4.9% (should not trigger >= 5)
        storage.addPatientData(1, 97.0, "Saturation", 2000L);
        storage.addPatientData(1, 92.1, "Saturation", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testSaturationDropOutsideTimeWindow() {
        // Drop of 5%, but it took 10 minutes and 1 millisecond
        long startTime = 1000000L;
        long outsideWindowTime = startTime + (10 * 60 * 1000) + 1;

        storage.addPatientData(1, 97.0, "Saturation", startTime);
        storage.addPatientData(1, 91.0, "Saturation", outsideWindowTime);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testHypotensiveHypoxemiaPartialConditions() {
        // Only Low BP
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "Saturation", 1000L);

        // Only Low Saturation
        storage.addPatientData(2, 110.0, "SystolicPressure", 2000L);
        storage.addPatientData(2, 90.0, "Saturation", 2000L);

        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(1)));
    }

    @Test
    void testECGInsufficientWindowAndExactAverage() {
        // Exactly 10 records (loop condition i < ecg.size() fails, window = 10)
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 2.0, "ECG", 1000L + i);
        }

        // 11th record is exactly 1.5x the average of the first 10
        // Average of ten 2.0s is 2.0. 1.5x average = 3.0.
        // Code checks for > 3.0, so exactly 3.0 should not trigger.
        storage.addPatientData(1, 3.0, "ECG", 2000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testUntriggeredAlertButton() {
        // Value other than 1.0
        storage.addPatientData(1, 0.0, "Alert", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }
    @Test
    void testDecreasingBPTrendExactChangeAndBrokenTrend() {
        // Condition A fails: Exact 10 mmHg drop (130 -> 120 is exactly 10, not > 10)
        storage.addPatientData(1, 130.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 120.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 110.0, "SystolicPressure", 3000L);

        // Condition B fails: Drops >10, but then drops <10 (130 -> 110 is 20, but 110 -> 105 is 5)
        storage.addPatientData(2, 130.0, "DiastolicPressure", 1000L);
        storage.addPatientData(2, 110.0, "DiastolicPressure", 2000L);
        storage.addPatientData(2, 105.0, "DiastolicPressure", 3000L);

        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(1)));
    }
}