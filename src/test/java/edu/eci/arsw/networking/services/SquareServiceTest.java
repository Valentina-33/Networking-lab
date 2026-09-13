package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquareServiceTest {

    private final SquareService service = new SquareService();

    @Test
    void integerValueIsSquaredCorrectly() {
        HttpResponse response = service.handle(Map.of("value", "7"));

        assertEquals(200, response.statusCode());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        assertEquals("{\"input\":7,\"square\":49}", body);
    }

    @Test
    void negativeValueIsSquaredToPositive() {
        HttpResponse response = service.handle(Map.of("value", "-3"));

        String body = new String(response.body(), StandardCharsets.UTF_8);
        assertEquals("{\"input\":-3,\"square\":9}", body);
    }

    @Test
    void decimalValueIsSupported() {
        HttpResponse response = service.handle(Map.of("value", "1.5"));

        String body = new String(response.body(), StandardCharsets.UTF_8);
        assertEquals("{\"input\":1.5,\"square\":2.25}", body);
    }

    @Test
    void missingValueIsBadRequest() {
        HttpResponse response = service.handle(Map.of());

        assertEquals(400, response.statusCode());
    }

    @Test
    void nonNumericValueIsBadRequest() {
        HttpResponse response = service.handle(Map.of("value", "not-a-number"));

        assertEquals(400, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("numeric"));
    }

    @Test
    void infiniteValueIsBadRequest() {
        HttpResponse response = service.handle(Map.of("value", "Infinity"));

        assertEquals(400, response.statusCode());
    }
}
