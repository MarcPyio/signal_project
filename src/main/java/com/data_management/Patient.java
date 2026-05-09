package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 * This class stores patient-specific data, allowing for the addition and
 * retrieval
 * of medical records based on specified criteria.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with a specified ID.
     * Initializes an empty list of patient records.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /**
     * Adds a new record to this patient's list of medical records.
     * The record is created with the specified measurement value, record type, and
     * timestamp. Duplicate records (exact same type and timestamp) are ignored.
     *
     * @param measurementValue the measurement value to store in the record
     * @param recordType       the type of record, e.g., "HeartRate",
     *                         "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since UNIX epoch
     */
    public synchronized void addRecord(double measurementValue, String recordType, long timestamp) {
        //Duplicate Check
        // We iterate backwards because if a duplicate arrives from the stream,
        for (int i = patientRecords.size() - 1; i >= 0; i--) {
            PatientRecord existingRecord = patientRecords.get(i);

            if (existingRecord.getTimestamp() == timestamp &&
                    existingRecord.getRecordType().equals(recordType)) {
                // We found an exact match for time and type. Ignore the duplicate.
                return;
            }
        }
        //Add the verified unique record
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        this.patientRecords.add(record);
    }

    /**
     * Retrieves a list of PatientRecord objects for this patient that fall within a
     * specified time range.
     * The method filters records based on the start and end times provided.
     *
     * @param startTime the start of the time range, in milliseconds since UNIX
     *                  epoch
     * @param endTime   the end of the time range, in milliseconds since UNIX epoch
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public synchronized List<PatientRecord> getRecords(long startTime, long endTime) {
        List<PatientRecord> filteredRecords = new ArrayList<>();
        for (PatientRecord record : this.patientRecords) {
            // Check if the record timestamp is between startTime and endTime
            if (record.getTimestamp() >= startTime && record.getTimestamp() <= endTime) {
                filteredRecords.add(record);
            }
        }
        return filteredRecords;
    }
    public int getPatientId(){
        return patientId;
    }
}
