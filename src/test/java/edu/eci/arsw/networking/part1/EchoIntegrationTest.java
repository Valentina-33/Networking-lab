package edu.eci.arsw.networking.part1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercises the preliminary EchoServer/EchoClient pair (networking guide,
 * Section 4) over a real socket, on an ephemeral port so it never collides
 * with another test or with the Part 2 application.
 */
@Timeout(10)
class EchoIntegrationTest {

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void echoesEachLineAndStopsOnBye() throws Exception {
        int port = findFreePort();
        Thread serverThread = new Thread(() -> {
            try {
                EchoServer.run(port);
            } catch (IOException e) {
                // Expected once the client sends "Bye." and the server returns.
            }
        }, "echo-server-test");
        serverThread.setDaemon(true);
        serverThread.start();

        // EchoServer.accept()s exactly one connection, so readiness cannot be
        // probed with a throwaway socket -- that probe would itself consume
        // the server's only accepted connection. Retry the real connection
        // instead, and keep whichever attempt succeeds.
        try (Socket socket = connectWithRetry(port);
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            out.println("Hello");
            assertEquals("Response: Hello", in.readLine());

            out.println("Bye.");
            assertEquals("Response: Bye.", in.readLine());
        }

        serverThread.join(5000);
    }

    private Socket connectWithRetry(int port) throws InterruptedException, IOException {
        long deadline = System.currentTimeMillis() + 5000;
        IOException lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                return new Socket("127.0.0.1", port);
            } catch (IOException e) {
                lastFailure = e;
                Thread.sleep(20);
            }
        }
        throw new IllegalStateException("EchoServer did not start listening in time", lastFailure);
    }
}
