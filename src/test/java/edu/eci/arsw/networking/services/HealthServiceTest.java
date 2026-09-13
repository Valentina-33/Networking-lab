package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthServiceTest {

    @Test
    void alwaysReportsUp() {
        HttpResponse response = new HealthService().handle();

        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=utf-8", response.contentType());
        assertEquals("{\"status\":\"UP\"}", new String(response.body(), StandardCharsets.UTF_8));
    }
}
