package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
/**
 * Outputs patient data over a TCP network connection.
 * * Usage: This class sets up a TCP server on a specified port and listens for a client connection.
 * Once a client connects, any generated patient data passed through the {@code output} method
 * is formatted as a comma-separated string and sent to the client over the network.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    /**
     * Constructs a new TcpOutputStrategy and initializes a TCP server.
     * The server listens for incoming client connections in a separate background thread
     * to prevent blocking the main execution thread of the simulation.
     *
     * @param port The port number on which the TCP server will listen for incoming connections.
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Formats and transmits the patient data to the connected TCP client.
     * If no client has connected yet, the method will ignore the output request.
     *
     * @param patientId the patient to output the data for.
     * @param timestamp The time at which the data was generated.
     * @param label The category or type of the data being outputted (e.g., "HeartRate", "BloodPressure", "Alert").
     * @param data The actual generated value of the patient, represented as a String.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
