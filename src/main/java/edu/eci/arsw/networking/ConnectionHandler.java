package edu.eci.arsw.networking;

import edu.eci.arsw.networking.http.HttpRequest;
import edu.eci.arsw.networking.http.HttpRequestParser;
import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.http.HttpResponseWriter;
import edu.eci.arsw.networking.http.MalformedRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles exactly one accepted client connection, start to finish: parse the
 * request, route it, write the response, close everything. The server loop
 * only starts the next {@code accept()} once this returns, which is what
 * keeps the whole application sequential.
 */
public final class ConnectionHandler {

    private static final Logger LOGGER = Logger.getLogger(ConnectionHandler.class.getName());

    private final RequestRouter router;

    public ConnectionHandler(RequestRouter router) {
        this.router = router;
    }

    public void handle(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {

            HttpRequest request;
            try {
                request = HttpRequestParser.parse(in);
            } catch (MalformedRequestException e) {
                LOGGER.log(Level.WARNING, "Malformed request from {0}: {1}",
                        new Object[]{socket.getRemoteSocketAddress(), e.getMessage()});
                HttpResponseWriter.write(out, HttpResponse.badRequestPlain("400 Bad Request"));
                return;
            }

            if (request == null) {
                // Client connected and disconnected without sending a request line.
                return;
            }

            LOGGER.info(() -> request.rawMethod() + " " + request.path()
                    + " " + request.version() + " from " + socket.getRemoteSocketAddress());

            HttpResponse response;
            try {
                response = router.route(request);
            } catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE, "Unhandled error while routing " + request.path(), e);
                response = HttpResponse.internalError();
            }

            HttpResponseWriter.write(out, response);
            HttpResponse sentResponse = response;
            LOGGER.info(() -> "-> " + sentResponse.statusCode() + " " + sentResponse.reasonPhrase());

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "I/O error while handling a connection", e);
        }
    }
}
