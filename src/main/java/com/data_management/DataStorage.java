package com.data_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system.
 * This class serves as a repository for all patient records, organized by
 * patient IDs.
 *
 * <p>Implemented as a thread-safe Singleton: only one instance exists for the
 * lifetime of the application. Obtain it via {@link #getInstance()}.
 */
public class DataStorage {
    private Map<Integer, Patient> patientMap;
    private static volatile DataStorage instance;

    /** Private constructor prevents direct instantiation. */
    private DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of DataStorage, creating it on the first call.
     *
     * <p>Thread-safe via double-checked locking.
     *
     * @return the singleton DataStorage instance
     */
    public static DataStorage getInstance() {
        if (instance == null) {
            synchronized (DataStorage.class) {
                if (instance == null) {
                    instance = new DataStorage();
                }
            }
        }
        return instance;
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage. Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate", "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        // computeIfAbsent checks for the key and creates the object atomically.
        // This prevents two threads from accidentally creating two different
        // Patient objects for the same ID at the exact same millisecond.
        Patient patient = patientMap.computeIfAbsent(patientId, k -> new Patient(k));

        // NOTE: You must also ensure that the `addRecord` method inside your
        // Patient class is thread-safe and checks for duplicates!
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix epoch
     * @return a list of PatientRecord objects that fall within the specified time range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Resets the singleton instance. For use in unit tests only —
     * do NOT call this in production code.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage, and continuously monitors
     * and evaluates patient data.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DataStorage storage = DataStorage.getInstance();

        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
        for (PatientRecord record : records) {
            System.out.println("Record for Patient ID: " + record.getPatientId() +
                    ", Type: " + record.getRecordType() +
                    ", Data: " + record.getMeasurementValue() +
                    ", Timestamp: " + record.getTimestamp());
        }

        AlertGenerator alertGenerator = new AlertGenerator(storage);

        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
}