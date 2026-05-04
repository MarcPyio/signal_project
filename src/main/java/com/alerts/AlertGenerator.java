package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        long now = System.currentTimeMillis();
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), 0, now);

        checkBloodPressure(patient, records);
        checkBloodSaturation(patient, records, now);
        checkHypotensiveHypoxemia(patient, records);
        checkECG(patient, records);
        checkTriggeredAlert(patient, records);
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("ALERT: Patient " + alert.getPatientId()
                + " | " + alert.getCondition()
                + " | Time: " + alert.getTimestamp());
    }

    /**
     * Checks blood pressure records for critical thresholds and trends.
     * Triggers alerts if systolic pressure exceeds 180 or drops below 90 mmHg,
     * or if diastolic pressure exceeds 120 or drops below 60 mmHg.
     *
     * @param patient the patient to evaluate
     * @param records all records for the patient
     */
    private void checkBloodPressure(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = filterByType(records, "SystolicPressure");
        List<PatientRecord> diastolic = filterByType(records, "DiastolicPressure");
        AlertFactory bpFactory = new BloodPressureAlertFactory(); // Instantiate the factory

        // Critical thresholds
        for (PatientRecord r : systolic) {


            if (r.getMeasurementValue() > 180) {
                Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), "Systolic BP Too High", r.getTimestamp());
                triggerAlert(myAlert);
            }
                if (r.getMeasurementValue() < 90) {
                    Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), "Systolic BP Too Low", r.getTimestamp());
                    triggerAlert(myAlert);
                }
        }
        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > 120){
                Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), "Diastolic BP Too High", r.getTimestamp());
                triggerAlert(myAlert);
            }
            if (r.getMeasurementValue() < 60){
                Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), "Diastolic BP Too Low", r.getTimestamp());
                triggerAlert(myAlert);
            }
        }

        // Trend alerts
        checkTrend(patient, systolic, "Systolic");
        checkTrend(patient, diastolic, "Diastolic");
    }

    /**
     * Checks for a consistent increasing or decreasing trend across three
     * consecutive blood pressure readings, where each changes by more than 10 mmHg.
     *
     * @param patient the patient to evaluate
     * @param records filtered blood pressure records (systolic or diastolic)
     * @param type    label for the type of pressure ("Systolic" or "Diastolic")
     */
    private void checkTrend(Patient patient, List<PatientRecord> records, String type) {
        AlertFactory bpFactory = new BloodPressureAlertFactory(); // Instantiate the factory
        for (int i = 2; i < records.size(); i++) {
            double a = records.get(i - 2).getMeasurementValue();
            double b = records.get(i - 1).getMeasurementValue();
            double c = records.get(i).getMeasurementValue();



            if (b - a > 10 && c - b > 10){
                Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), type + " BP Increasing", records.get(i).getTimestamp());
                triggerAlert(myAlert);}
            if (a - b > 10 && b - c > 10) {
                Alert myAlert = bpFactory.createAlert(String.valueOf(patient.getPatientId()), type + " BP Decreasing", records.get(i).getTimestamp());
                triggerAlert(myAlert);
            }
        }
    }

    /**
     * Checks blood oxygen saturation records for low saturation (below 92%)
     * and rapid drops of 5% or more within a 10-minute window.
     *
     * @param patient    the patient to evaluate
     * @param records    all records for the patient
     * @param now        the current time in milliseconds
     */
    private void checkBloodSaturation(Patient patient, List<PatientRecord> records, long now) {
        List<PatientRecord> satRecords = filterByType(records, "Saturation");
        AlertFactory bsFactory = new BloodOxygenAlertFactory(); // Instantiate the factory

        for (PatientRecord r : satRecords) {
            // Low saturation alert
            if (r.getMeasurementValue() < 92){
                Alert myAlert = bsFactory.createAlert(String.valueOf(patient.getPatientId()), "Low Blood Saturation", r.getTimestamp());
                triggerAlert(myAlert);
            }
            // Rapid drop alert - check within 10 minute window
            long tenMinAgo = r.getTimestamp() - (10 * 60 * 1000);
            for (PatientRecord prev : satRecords) {
                if (prev.getTimestamp() >= tenMinAgo && prev.getTimestamp() < r.getTimestamp()) {
                    if (prev.getMeasurementValue() - r.getMeasurementValue() >= 5) {
                        Alert myAlert = bsFactory.createAlert(String.valueOf(patient.getPatientId()), "Rapid Saturation Drop", r.getTimestamp());
                        triggerAlert(myAlert);
                    }
                }
            }
        }
    }

    /**
     * Checks for Hypotensive Hypoxemia by detecting if the patient simultaneously
     * has a systolic blood pressure below 90 mmHg and blood oxygen saturation
     * below 92%.
     *
     * @param patient the patient to evaluate
     * @param records all records for the patient
     */
    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
        boolean lowBP = false;
        boolean lowSat = false;

        AlertFactory hhFactory = new HypotensiveHypoxemiaAlertFactory();

        for (PatientRecord r : filterByType(records, "SystolicPressure"))
            if (r.getMeasurementValue() < 90) lowBP = true;

        for (PatientRecord r : filterByType(records, "Saturation"))
            if (r.getMeasurementValue() < 92) lowSat = true;

        // Trigger alert only if both conditions are met simultaneously
        if (lowBP && lowSat){
            Alert myAlert = hhFactory.createAlert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia", System.currentTimeMillis());
            triggerAlert(myAlert);
        }
    }

    /**
     * Checks ECG data for abnormal peaks using a sliding window average.
     * Triggers an alert if a reading exceeds 1.5x the current window average.
     *
     * @param patient the patient to evaluate
     * @param records all records for the patient
     */
    private void checkECG(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecg = filterByType(records, "ECG");
        AlertFactory ecFactory = new ECGAlertFactory();
        int window = 10;

        for (int i = window; i < ecg.size(); i++) {
            // Calculate sliding window average
            double avg = 0;
            for (int j = i - window; j < i; j++)
                avg += ecg.get(j).getMeasurementValue();
            avg /= window;

            if (ecg.get(i).getMeasurementValue() > avg * 1.5){
                Alert myAlert = ecFactory.createAlert(String.valueOf(patient.getPatientId()), "Abnormal ECG Peak", ecg.get(i).getTimestamp());
                triggerAlert(myAlert);
            }
        }
    }

    /**
     * Checks for manually triggered alerts from nurses or patients pressing
     * the alert button. A measurement value of 1.0 indicates a triggered alert.
     *
     * @param patient the patient to evaluate
     * @param records all records for the patient
     */
    private void checkTriggeredAlert(Patient patient, List<PatientRecord> records) {
        AlertFactory manualFactory = new ManualAlertFactory();
        for (PatientRecord r : filterByType(records, "Alert"))
            if (r.getMeasurementValue() == 1.0){
                Alert myAlert = manualFactory.createAlert(String.valueOf(patient.getPatientId()), "Triggered Alert", r.getTimestamp());
                triggerAlert(myAlert);
            }
    }

    /**
     * Helper method to filter a list of patient records by record type.
     *
     * @param records the full list of records
     * @param type    the record type to filter by
     * @return a filtered list containing only records of the specified type
     */
    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records)
            if (r.getRecordType().equals(type))
                result.add(r);
        return result;
    }
}