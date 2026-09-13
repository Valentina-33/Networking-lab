package edu.eci.arsw.networking.part1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Preliminary exercise (networking guide, Section 4.2, Figure 4) — not part
 * of the graded Part 2 deliverable, kept here as evidence that the
 * prerequisite material was completed before Part 2 began.
 *
 * <p>Accepts one client connection, echoes back every line it receives
 * prefixed with "Response: ", and stops once the client sends
 * {@code "Bye."} or disconnects.</p>
 */
public final class EchoServer {

    private static final int DEFAULT_PORT = 36000;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        run(port);
    }

    static void run(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Echo server ready on port " + port);

            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(
                         new java.io.OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Message: " + inputLine);
                    String outputLine = "Response: " + inputLine;
                    out.println(outputLine);
                    if (outputLine.equals("Response: Bye.")) {
                        break;
                    }
                }
            }
        }
    }
}
