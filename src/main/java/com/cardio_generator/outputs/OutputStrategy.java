package com.cardio_generator.outputs;
/**
 * Sets a baseline for outputting patient data in the health monitoring simulation.
 * * Usage: Classes implementing this interface will handle the specific mechanisms
 * of data output, such as writing to a local file, saving to a database, or transmitting
 * over a network socket.
 */
public interface OutputStrategy {
    /**
     * Outputs the generated simulation data for a specific patient.
     *
     * @param patientId the patient to output the data for.
     * @param timestamp The time at which the data was generated.
     * @param label The category or type of the data being outputted (e.g., "HeartRate", "BloodPressure", "Alert").
     * @param data The actual generated value of the patient, represented as a String.
     */
    void output(int patientId, long timestamp, String label, String data);
}