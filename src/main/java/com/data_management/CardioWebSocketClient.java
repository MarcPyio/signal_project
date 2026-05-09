package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.BlockingQueue;

/**
 * A WebSocket client designed to connect to the cardio generator simulation.
 * It acts as the "Producer" in a Producer-Consumer architecture, listening
 * for live data streams and safely handing them off to a shared queue.
 */
public class CardioWebSocketClient extends WebSocketClient {

    // A thread-safe queue used to pass messages from this asynchronous
    // WebSocket thread to the main processing thread.
    private final BlockingQueue<String> messageQueue;

    public CardioWebSocketClient(URI serverUri, BlockingQueue<String> queue) {
        super(serverUri);
        this.messageQueue = queue;
    }

    /**
     * Triggered every time the server broadcasts a new piece of data.
     *
     * @param message The raw CSV string broadcasted by the server.
     */
    @Override
    public void onMessage(String message) {
        /*
         * DATA PARSING & ERROR MANAGEMENT:
         * We expect a strict CSV format: "patientId,timestamp,label,data".
         * Before offering the message to the queue, we perform a basic structural
         * check to ensure it has exactly 4 parts. If a network glitch causes a
         * corrupted or incomplete string, we drop it here to protect the DataReader
         * from a NullPointerException or ArrayIndexOutOfBoundsException later.
         */
        if (message != null && message.split(",").length == 4) {
            // offer() inserts the element into the queue without blocking.
            messageQueue.offer(message);
        } else {
            System.err.println("Dropped corrupted message: " + message);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to cardio stream!");
    }

    /**
     * Triggered when the connection is closed, either intentionally or due to an error.
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection lost. Reason: " + reason);

        /*
         * ERROR MANAGEMENT: Auto-Reconnection
         * If 'remote' is true, it means the server closed the connection (or the network
         * dropped), not us. We spin up a temporary background thread to wait 5 seconds
         * and attempt to reconnect, ensuring the system is fault-tolerant.
         */
        if (remote) {
            System.out.println("Attempting to reconnect in 5 seconds...");
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    this.reconnect(); // Java-WebSocket's built-in reconnection method
                } catch (InterruptedException e) {
                    // If the thread is interrupted while sleeping, clean up gracefully
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket Client Error occurred:");
        ex.printStackTrace();
    }
}