package com.data_management;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implements the DataReader interface to process real-time WebSocket streams.
 * It acts as the "Consumer", pulling raw strings from a queue, parsing them
 * into strongly-typed variables, and storing them in DataStorage.
 */
public class WebSocketDataReader implements DataReader {

    private final URI serverUri;

    public WebSocketDataReader(URI serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * Connects to the WebSocket stream and continuously reads incoming data.
     * WARNING: Because this method contains an infinite loop, it MUST be
     * executed inside a dedicated background thread, or it will block the application.
     *
     * @param dataStorage the storage where parsed data will be saved
     * @throws IOException if parsing fails or the thread is interrupted
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        /*
         * ARCHITECTURE SETUP:
         * We initialize a LinkedBlockingQueue. This acts as the secure bridge
         * between the WebSocketClient (which runs on its own async thread)
         * and this DataReader loop.
         */
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        CardioWebSocketClient client = new CardioWebSocketClient(serverUri, queue);

        // Initiate the asynchronous connection
        client.connect();

        try {
            // Enter a continuous loop to act as a permanent listener
            while (true) {
                /*
                 * queue.take() is a blocking call. If the queue is empty, this loop
                 * will pause here and consume zero CPU cycles until the WebSocketClient
                 * puts a new message into the queue.
                 */
                String message = queue.take();

                /*
                 * DATA PARSING:
                 * We know from the OutputStrategy that the format is:
                 * PatientId (int), Timestamp (long), Label (String), Data (double)
                 */
                String[] parts = message.split(",");
                    try {
                        // Convert the string parts into their strict mathematical types
                        int patientId = Integer.parseInt(parts[0].trim());
                        long timestamp = Long.parseLong(parts[1].trim());
                        String label = parts[2].trim();
                        double measurementValue = Double.parseDouble(parts[3].trim());

                        // Push the verified, typed data into the storage singleton
                        dataStorage.addPatientData(patientId, measurementValue, label, timestamp);

                    } catch (NumberFormatException e) {
                        /*
                         * ERROR MANAGEMENT:
                         * If a string cannot be parsed into a number (e.g., "abc" into an int),
                         * we catch the exception here so it doesn't break the infinite loop.
                         * We log it and let the loop continue to the next message.
                         */
                        System.err.println("Failed to parse numeric data from message: " + message);
                    }
            }
        } catch (InterruptedException e) {
            /*
             * ERROR MANAGEMENT: Graceful Shutdown
             * If the main application decides to shut down this background thread,
             * it will trigger an InterruptedException. We catch it, close the
             * WebSocket connection cleanly, and restore the interrupt flag.
             */
            Thread.currentThread().interrupt();
            client.close();
            throw new IOException("WebSocket reading was interrupted by the system", e);
        } catch (Exception e) {
            // Catch-all for any unforeseen fatal errors during parsing
            throw new IOException("Fatal error parsing WebSocket data stream", e);
        }
    }
}