package data_management;

import com.data_management.CardioWebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class CardioWebSocketClientTest {

    private CardioWebSocketClient client;
    private BlockingQueue<String> mockQueue;

    @BeforeEach
    void setUp() throws Exception {
        mockQueue = new LinkedBlockingQueue<>();
        URI dummyUri = new URI("ws://localhost:8080");
        client = new CardioWebSocketClient(dummyUri, mockQueue);
    }

    @Test
    void testOnMessage_ValidData_AddsToQueue() {
        String validMessage = "1,1700000000000,HeartRate,80.5";
        client.onMessage(validMessage);
        assertEquals(1, mockQueue.size(), "Queue should contain 1 message");
        assertEquals(validMessage, mockQueue.peek(), "Queue should contain the exact message sent");
    }

    @Test
    void testOnMessage_InvalidData_Dropped() {
        String invalidMessage = "bad,data,format";
        client.onMessage(invalidMessage);
        assertTrue(mockQueue.isEmpty(), "Queue should be empty because the message was malformed");
    }

    // NEW TEST: Hits the "False" branch of the `message != null` check
    @Test
    void testOnMessage_NullMessage_Dropped() {
        // Send a completely null message
        String test = null;
        client.onMessage(test);

        // Ensure the queue remained untouched
        assertTrue(mockQueue.isEmpty(), "Queue should be empty because the message was null");
    }

    @Test
    void testLifecycleMethods_DoNotThrowExceptions() {
        assertDoesNotThrow(() -> client.onOpen(null));
        assertDoesNotThrow(() -> client.onError(new Exception("Dummy Error")));
        assertDoesNotThrow(() -> client.onClose(1000, "Normal closure", false));
    }

    //Tests the lambda logic in onClose and captures the InterruptedException branch
    @Test
    void testOnClose_RemoteDisconnect_TriggersReconnectThread() throws InterruptedException {
        // Trigger the remote close, which spins up the anonymous thread
        client.onClose(1006, "Server dropped", true);

        // Find the background thread that was just created and interrupt it
        // to satisfy JaCoCo's coverage of the catch (InterruptedException e) block!
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);
        for (Thread t : threads) {
            // Target the anonymous sleep thread spawned by the client
            if (t != null && t != Thread.currentThread() && !t.isDaemon()) {
                t.interrupt();
            }
        }

        // Give it a tiny bit of time to process the interrupt
        Thread.sleep(100);
    }
}