package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
/**
 * An output strategy that writes patient data to local text files.
 * * Usage: This class creates a directory and writes data into separate
 * text files categorized by the data's label (e.g., "Alert.txt", "HeartRate.txt").
 */
public class FileOutputStrategy implements OutputStrategy {

    private String baseDirectory; //changed from uppercase to lowercase lowerCamelCase naming convention

    /**
     * A map used to cache and retrieve the absolute file paths associated with each data label.
     */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>(); //change file_map to fileMap lowerCamelCase naming convention
    /**
     * Constructs a new FileOutputStrategy with the specified base directory for file storage.
     * @param baseDirectory The path to the directory where the output text files will be created and stored.
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }
    /**
     * Outputs the patient data by adding it to a file of the data's label.
     * If the  directory or the text file does not exist, they are created automatically.
     *
     * @param patientId The specific patient.
     * @param timestamp The time at which the data was generated.
     * @param label The category or type of the data being outputted (e.g., "HeartRate", "BloodPressure", "Alert").
     * @param data The actual generated value of the patient, represented as a String.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        //Changed local variable name 'FilePath' to 'filePath' to adhere to lowerCamelCase naming convention
        // Updated map and directory references to match their corrected lowerCamelCase names
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (IOException e) { //specify which exception to catch so
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}