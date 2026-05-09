package data_management;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileDataReaderTest {

    // JUnit 5 automatically creates this temp directory before each test
    // and deletes it entirely after the test finishes.
    @TempDir
    Path tempDir;

    private DataStorage storage;
    private FileDataReader reader;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();

        // Point the reader to our automatically managed temporary directory
        reader = new FileDataReader(tempDir.toString());
    }

    @Test
    void testReadData_ValidFiles_SuccessfullyParses() throws IOException {
        // Arrange: Create a valid text file with perfectly formatted data
        Path validFile = tempDir.resolve("patient_data.txt");
        String content = "Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 72.0\n" +
                "Patient ID: 1, Timestamp: 1714376789051, Label: BloodPressure, Data: 120.0";
        Files.writeString(validFile, content);

        // Act
        reader.readData(storage);

        // Assert: The valid data should successfully make it into DataStorage
        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size(), "Should parse two valid records");
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("BloodPressure", records.get(1).getRecordType());
    }

    @Test
    void testReadData_SkipsNonTxtAndDirectories_HitsFilterBranches() throws IOException {
        // Arrange 1: Create a .csv file (Tests the endsWith(".txt") filter)
        Path csvFile = tempDir.resolve("data.csv");
        Files.writeString(csvFile, "Patient ID: 2, Timestamp: 12345, Label: HeartRate, Data: 80.0");

        // Arrange 2: Create a directory that happens to end in .txt
        // (Tests the isRegularFile filter)
        Path subDir = tempDir.resolve("sneaky_folder.txt");
        Files.createDirectory(subDir);

        // Act
        reader.readData(storage);

        // Assert: Neither the CSV nor the directory should be parsed
        List<PatientRecord> records = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty(), "Should ignore non-txt files and directories entirely");
    }

    @Test
    void testParseFile_MalformedLines_CaughtAndSkipped() throws IOException {
        // Arrange: Create a file with a mix of good and bad data
        Path mixedFile = tempDir.resolve("mixed_data.txt");
        String content = "Patient ID: 3, Timestamp: 1714376789050, Label: HeartRate, Data: 72.0\n" +
                "Completely broken line with no formatting\n" +
                "Patient ID: 3, Timestamp: ABC, Label: BadTime, Data: 12.0\n" +
                "Patient ID: 3, Timestamp: 1714376789055, Label: BloodPressure, Data: 120.0";
        Files.writeString(mixedFile, content);

        // Act
        reader.readData(storage);

        // Assert: The generic catch(Exception e) block in parseFile should swallow
        // the errors for lines 2 and 3, but successfully parse lines 1 and 4.
        List<PatientRecord> records = storage.getRecords(3, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size(), "Should only parse the 2 valid lines and skip the malformed ones");
    }

    @Test
    void testReadData_IOExceptionInsideLambda_CaughtProperly() throws IOException {
        // Arrange: Create a valid file, but explicitly remove read permissions from it.
        // When FileReader attempts to open it, it will throw an IOException, hitting
        // the specific catch block inside the forEach lambda loop.
        Path unreadableFile = tempDir.resolve("unreadable.txt");
        Files.writeString(unreadableFile, "Patient ID: 4, Timestamp: 123, Label: HR, Data: 70.0");
        unreadableFile.toFile().setReadable(false);

        // Act
        reader.readData(storage);

        // Assert
        List<PatientRecord> records = storage.getRecords(4, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty(), "Should gracefully catch the IOException and skip the file");

        // Clean up: Restore permissions so JUnit can successfully delete the temp folder after the test
        unreadableFile.toFile().setReadable(true);
    }

    @Test
    void testReadData_EmptyFile_HandledGracefully() throws IOException {
        // Arrange: Test the while loop condition in parseFile with an empty file
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        // Act
        reader.readData(storage);

        // Assert
        List<Patient> patients = storage.getAllPatients();
        assertTrue(patients.isEmpty(), "Empty file should not create any patient records");
    }

    @Test
    void testReadData_InvalidDirectory_ThrowsIOException() {
        // Arrange: Point the reader at a directory path that does not exist
        FileDataReader badDirReader = new FileDataReader(tempDir.resolve("missing_folder").toString());

        // Act & Assert: Files.walk() will throw a top-level IOException
        assertThrows(IOException.class, () -> {
            badDirReader.readData(storage);
        }, "Should throw an IOException if the root directory does not exist");
    }
}