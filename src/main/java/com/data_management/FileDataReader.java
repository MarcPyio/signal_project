package com.data_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileDataReader implements DataReader {
    private String directory;

    public FileDataReader(String directory) {
        this.directory = directory;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dirPath = Paths.get(directory);
        try (Stream<Path> files = Files.walk(dirPath)) {
            files.filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".txt"))
                    .forEach(file -> {
                        try {
                            parseFile(file, dataStorage);
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file);
                        }
                    });
        }
    }

    private void parseFile(Path file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Expected format: Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 72.0
                try {
                    String[] parts = line.split(", ");
                    int patientId = Integer.parseInt(parts[0].split(": ")[1].trim());
                    long timestamp = Long.parseLong(parts[1].split(": ")[1].trim());
                    String label = parts[2].split(": ")[1].trim();
                    double data = Double.parseDouble(parts[3].split(": ")[1].trim());

                    dataStorage.addPatientData(patientId, data, label, timestamp);
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
    }
}
