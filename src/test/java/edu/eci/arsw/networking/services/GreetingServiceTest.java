package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreetingServiceTest {

    private final GreetingService service = new GreetingService();

    @Test
    void validNameProducesGreetingJson() {
        HttpResponse response = service.handle(Map.of("name", "Ada"));

        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=utf-8", response.contentType());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("\"name\":\"Ada\""));
        assertTrue(body.contains("\"message\":\"Hello, Ada!\""));
    }

    @Test
    void missingNameIsBadRequest() {
        HttpResponse response = service.handle(Map.of());

        assertEquals(400, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("error"));
    }

    @Test
    void blankNameIsBadRequest() {
        HttpResponse response = service.handle(Map.of("name", "   "));

        assertEquals(400, response.statusCode());
    }

    @Test
    void untrustedNameIsEscapedInsteadOfBreakingJson() {
        HttpResponse response = service.handle(Map.of("name", "\", \"admin\": true, \"x\": \""));

        assertEquals(200, response.statusCode());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        // The malicious value must show up escaped (as \"), never as a raw
        // closing quote that would let it terminate the string early and
        // inject a sibling JSON field.
        assertTrue(body.contains("\\\"admin\\\": true"));
        assertTrue(!body.contains("\"admin\": true"));
    }
}
