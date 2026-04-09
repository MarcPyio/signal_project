package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generator for simulating patient alert states.
 *
 * <p>Manages the state of alerts for each patient, simulating the triggering
 * and resolution of alerts based on probabilistic distributions.
 */

public class AlertGenerator implements PatientDataGenerator {
    //static final variables are treated as constants using uppercase letters and seperated by underscores
    public static final Random RANDOM_GENERATOR = new Random();
    //change alertStates to lowerCamelCase
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs a new alert generator for the specified number of patients.
     *
     * @param patientCount the total number of patients to monitor
     */

    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates and outputs alert state transitions for a specific patient.
     *
     * <p>If an alert is active, it has a high probability of resolving. If inactive,
     * a Poisson-based probability determines if a new alert is triggered.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the mechanism used to output the alert status
     */

    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try { //refactor alerStates to all instances
            if (alertStates[patientId]) {
                //refactor RANDOM_GENERATOR
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                //lowerCamelCase Lambda changed to lambda
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
