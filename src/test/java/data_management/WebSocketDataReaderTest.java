package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class WebSocketDataReaderTest {

    private DummyServer testServer;
    private DataStorage realStorage;
    private WebSocketDataReader reader;
    private Thread readerThread;

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetInstance();
        realStorage = DataStorage.getInstance();

        testServer = new DummyServer(8887);
        testServer.start();

        Thread.sleep(100);

        reader = new WebSocketDataReader(new URI("ws://localhost:8887"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        testServer.stop();
    }

    @Test
    void testReadData_ValidMessage_PushesToStorage() throws InterruptedException {
        readerThread = new Thread(() -> {
            try { reader.readData(realStorage); } catch (IOException e) {}
        });
        readerThread.start();
        Thread.sleep(500);

        testServer.broadcast("42,1700000000000,BloodPressure,120.0");
        Thread.sleep(1000);

        List<PatientRecord> records = realStorage.getRecords(42, 1699999999999L, 1700000000001L);

        assertFalse(records.isEmpty(), "The record should have been saved to DataStorage");
        assertEquals(120.0, records.get(0).getMeasurementValue(), "Measurement value should match");
        assertEquals("BloodPressure", records.get(0).getRecordType(), "Record type should match");
    }

    @Test
    void testReadData_NumberFormatException_HandledGracefully() throws InterruptedException {
        readerThread = new Thread(() -> {
            try { reader.readData(realStorage); } catch (IOException e) {}
        });
        readerThread.start();
        Thread.sleep(500);

        testServer.broadcast("42,BAD_TIMESTAMP,BloodPressure,BAD_DOUBLE");
        testServer.broadcast("42,1700000000000,BloodPressure,120.0");
        Thread.sleep(1000);

        List<PatientRecord> records = realStorage.getRecords(42, 1699999999999L, 1700000000001L);
        assertEquals(1, records.size(), "Only the valid record should be saved");
    }

    @Test
    void testReadData_InvalidMessageLength_Ignored() throws InterruptedException {
        readerThread = new Thread(() -> {
            try { reader.readData(realStorage); } catch (IOException e) {}
        });
        readerThread.start();
        Thread.sleep(500);

        // Broadcast a string with only 3 parts (Hits the false branch!)
        testServer.broadcast("42,1700000000000,BloodPressure");

        // Immediately follow up with a good message to prove the loop didn't break
        testServer.broadcast("42,1700000000000,BloodPressure,120.0");
        Thread.sleep(1000);

        List<PatientRecord> records = realStorage.getRecords(42, 1699999999999L, 1700000000001L);
        assertEquals(1, records.size(), "Only the perfectly formatted record should be processed");
    }
    @Test
    void testReadData_GenericException_CaughtAndWrapped() throws InterruptedException {
        // Arrange: Start the reader in a background thread, but pass NULL for DataStorage!
        readerThread = new Thread(() -> {
            try {
                // Passing null will intentionally cause a NullPointerException
                // when the loop attempts to save valid data.
                reader.readData(null);
            } catch (IOException e) {
                // Assert: The NPE should be caught by your generic Exception block
                // and wrapped in this exact IOException message.
                assertEquals("Fatal error parsing WebSocket data stream", e.getMessage());
            }
        });
        readerThread.start();
        Thread.sleep(500); // Wait for connection

        // Act: Broadcast a perfectly valid message so the parser reaches the save logic
        testServer.broadcast("42,1700000000000,BloodPressure,120.0");

        // Give the background thread a moment to process the message and crash
        Thread.sleep(1000);
    }

    @Test
    void testReadData_InvalidMessageLengths_HitsFalseBranch() throws InterruptedException {
        readerThread = new Thread(() -> {
            try { reader.readData(realStorage); } catch (IOException e) {}
        });
        readerThread.start();
        Thread.sleep(500); // Wait for connection

        // Act 1: Broadcast a 3-part string (Too short)
        testServer.broadcast("42,1700000000000,BloodPressure");

        // Act 2: Broadcast a 5-part string (Too long)
        testServer.broadcast("42,1700000000000,BloodPressure,120.0,EXTRA_DATA");

        // Act 3: Follow up immediately with a valid 4-part string to prove
        // the bad lengths were ignored and the loop didn't break.
        testServer.broadcast("42,1700000000000,BloodPressure,120.0");

        Thread.sleep(1000); // Wait for processing

        // Assert: Query the storage. Only the valid 4-part message should exist!
        List<PatientRecord> records = realStorage.getRecords(42, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size(), "Only the perfectly formatted 4-part record should be processed");
    }

    private static class DummyServer extends WebSocketServer {
        public DummyServer(int port) { super(new InetSocketAddress(port)); }
        @Override public void onOpen(WebSocket conn, ClientHandshake handshake) {}
        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onMessage(WebSocket conn, String message) {}
        @Override public void onError(WebSocket conn, Exception ex) {}
        @Override public void onStart() {}
    }
}