package edu.eci.arsw.networking.part1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Preliminary exercise (networking guide, Section 4.1, Figure 3) — not part
 * of the graded Part 2 deliverable, kept here as evidence that the
 * prerequisite material was completed before Part 2 began.
 *
 * <p>Connects to {@link EchoServer}, sends every line typed on the keyboard,
 * and prints the echoed response.</p>
 */
public final class EchoClient {

    private static final int DEFAULT_PORT = 36000;

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader keyboard = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.println("Connected to echo server at " + host + ":" + port
                    + ". Type a line and press Enter (\"Bye.\" ends the session).");

            String userInput;
            while ((userInput = keyboard.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
                if (userInput.equals("Bye.")) {
                    break;
                }
            }
        }
    }
}
