package edu.eci.arsw.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the lab's sequential HTTP mini web application.
 *
 * <p>The lifecycle is intentionally simple: open one listening socket, then
 * loop forever accepting one client connection, handling it completely, and
 * only then accepting the next one. No thread, thread pool, or executor is
 * introduced anywhere in this class or in {@link ConnectionHandler} &mdash;
 * that limitation is the point of this baseline.</p>
 */
public final class NetworkingServer {

    private static final Logger LOGGER = Logger.getLogger(NetworkingServer.class.getName());
    private static final int DEFAULT_PORT = 35000;

    private final int port;
    private volatile ServerSocket serverSocket;

    public NetworkingServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        RequestRouter router = new RequestRouter();
        ConnectionHandler connectionHandler = new ConnectionHandler(router);

        // Binding with no explicit address makes the socket listen on every
        // network interface, not just loopback, which is what lets the
        // server accept connections once it runs on an EC2 instance.
        serverSocket = new ServerSocket(port);
        LOGGER.info(() -> "Networking lab server listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "server-shutdown-hook"));

        while (!serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketException e) {
                if (serverSocket.isClosed()) {
                    LOGGER.info("Server socket closed, stopping accept loop.");
                    break;
                }
                throw e;
            }
            connectionHandler.handle(clientSocket);
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Server socket closed.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while closing the server socket", e);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = resolvePort();
        new NetworkingServer(port).start();
    }

    private static int resolvePort() {
        String fromProperty = System.getProperty("port");
        String fromEnv = System.getenv("PORT");
        String raw = fromProperty != null ? fromProperty : fromEnv;
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> "Invalid port '" + raw + "', falling back to default " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
