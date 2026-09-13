package edu.eci.arsw.networking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end test that runs the real server over a real socket, exercising
 * the full accept -> parse -> route -> respond -> close cycle the way a
 * browser would, including several requests in a row against the same
 * running process (section 6.1 of the lab).
 */
@Timeout(30)
class NetworkingServerIntegrationTest {

    private NetworkingServer server;
    private Thread serverThread;
    private int port;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws IOException, InterruptedException {
        port = findFreePort();
        server = new NetworkingServer(port);
        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                // Expected once stop() closes the socket during teardown.
            }
        }, "test-server");
        serverThread.setDaemon(true);
        serverThread.start();
        awaitServerReady();
    }

    @AfterEach
    void stopServer() throws InterruptedException {
        server.stop();
        serverThread.join(5000);
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void awaitServerReady() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            try {
                get("/api/health");
                return;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
        throw new IllegalStateException("Server did not become ready in time");
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void serverAcceptsManyConsecutiveRequestsWithoutRestarting() throws Exception {
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> response = get("/api/health");
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("UP"));
        }
    }

    @Test
    void homePageAndStaticAssetsAreServedWithCorrectContentTypes() throws Exception {
        HttpResponse<String> home = get("/");
        assertEquals(200, home.statusCode());
        assertTrue(home.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));

        HttpResponse<String> script = get("/app.js");
        assertEquals(200, script.statusCode());
        assertTrue(script.headers().firstValue("Content-Type").orElse("").startsWith("text/javascript"));
    }

    @Test
    void greetingServiceRoundTrips() throws Exception {
        HttpResponse<String> response = get("/api/greeting?name=Ada");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Ada"));
    }

    @Test
    void invalidSquareRequestReturnsBadRequest() throws Exception {
        HttpResponse<String> response = get("/api/square?value=abc");
        assertEquals(400, response.statusCode());
    }

    @Test
    void unsupportedMethodIsRejected() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/health"))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
    }

    @Test
    void pathTraversalAttemptIsRejected() throws Exception {
        // Sent over a raw socket so the request line reaches the server
        // byte-for-byte, bypassing any client-side URI normalization that
        // could otherwise mask what we are actually testing here.
        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            out.write("GET /../pom.xml HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String statusLine = in.readLine();
            assertTrue(statusLine.startsWith("HTTP/1.1 400"));
        }
    }
}
