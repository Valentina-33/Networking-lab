package edu.eci.arsw.networking;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticResourceHandlerTest {

    private final StaticResourceHandler handler = new StaticResourceHandler();

    @Test
    void rootPathServesIndexHtml() {
        HttpResponse response = handler.handle("/");

        assertEquals(200, response.statusCode());
        assertEquals("text/html; charset=utf-8", response.contentType());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("<title>"));
    }

    @Test
    void servesJavascriptWithCorrectContentType() {
        HttpResponse response = handler.handle("/app.js");

        assertEquals(200, response.statusCode());
        assertEquals("text/javascript; charset=utf-8", response.contentType());
    }

    @Test
    void servesPngImageAsBytesWithCorrectContentTypeAndLength() {
        HttpResponse response = handler.handle("/images/logo.png");

        assertEquals(200, response.statusCode());
        assertEquals("image/png", response.contentType());
        assertTrue(response.body().length > 0);
        // PNG magic number.
        assertEquals((byte) 0x89, response.body()[0]);
        assertEquals('P', response.body()[1]);
        assertEquals('N', response.body()[2]);
        assertEquals('G', response.body()[3]);
    }

    @Test
    void servesJpegImageWithCorrectContentType() {
        HttpResponse response = handler.handle("/images/banner.jpg");

        assertEquals(200, response.statusCode());
        assertEquals("image/jpeg", response.contentType());
        assertEquals((byte) 0xFF, response.body()[0]);
        assertEquals((byte) 0xD8, response.body()[1]);
    }

    @Test
    void missingResourceIsNotFound() {
        HttpResponse response = handler.handle("/nope.html");

        assertEquals(404, response.statusCode());
    }

    @Test
    void traversalAttemptIsRejectedBeforeTouchingTheFilesystem() {
        HttpResponse response = handler.handle("/../pom.xml");

        assertEquals(400, response.statusCode());
    }

    @Test
    void deepTraversalAttemptIsRejected() {
        HttpResponse response = handler.handle("/images/../../../../pom.xml");

        assertEquals(400, response.statusCode());
    }
}
