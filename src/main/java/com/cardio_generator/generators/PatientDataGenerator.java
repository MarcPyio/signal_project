package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating synthetic health data for individual patients.
 *
 * <p>Implementations are responsible for simulating specific health metrics
 * and routing the results to a provided output destination.
 */

public interface PatientDataGenerator {

    /**
     * Generates simulated data for a specific patient and sends it to the output.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the mechanism used to output the generated data
     */

    void generate(int patientId, OutputStrategy outputStrategy);
}
