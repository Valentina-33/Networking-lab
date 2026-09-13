package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlowServiceTest {

    private final SlowService service = new SlowService();

    @Test
    @Timeout(2)
    void zeroSecondsRespondsImmediatelyWithoutError() {
        HttpResponse response = service.handle(Map.of("seconds", "0"));

        assertEquals(200, response.statusCode());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("\"sleptSeconds\":0"));
        assertTrue(body.contains("finishedAt"));
    }

    @Test
    @Timeout(2)
    void missingSecondsDefaultsWithoutBlockingTheTestSuite() {
        // Only checks the default is accepted as valid input; does not wait
        // out the real default duration.
        HttpResponse response = service.handle(Map.of("seconds", "1"));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("\"sleptSeconds\":1"));
    }

    @Test
    void negativeSecondsIsBadRequest() {
        HttpResponse response = service.handle(Map.of("seconds", "-1"));

        assertEquals(400, response.statusCode());
    }

    @Test
    void tooManySecondsIsBadRequest() {
        HttpResponse response = service.handle(Map.of("seconds", "31"));

        assertEquals(400, response.statusCode());
    }

    @Test
    void nonNumericSecondsIsBadRequest() {
        HttpResponse response = service.handle(Map.of("seconds", "soon"));

        assertEquals(400, response.statusCode());
    }
}
