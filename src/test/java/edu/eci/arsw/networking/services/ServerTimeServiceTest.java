package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerTimeServiceTest {

    private static final Pattern SERVER_TIME_FIELD = Pattern.compile("\"serverTime\":\"([^\"]+)\"");

    @Test
    void reportsTimeFromTheInjectedClockNotTheSystemClock() {
        Instant fixedInstant = Instant.parse("2026-01-01T00:00:01Z");
        Clock fixed = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        ServerTimeService service = new ServerTimeService(fixed);

        HttpResponse response = service.handle();

        assertEquals(200, response.statusCode());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        Matcher matcher = SERVER_TIME_FIELD.matcher(body);
        assertTrue(matcher.find(), "Response should contain a serverTime field: " + body);
        assertEquals(fixedInstant, OffsetDateTime.parse(matcher.group(1)).toInstant());
    }

    @Test
    void responseIsJson() {
        ServerTimeService service = new ServerTimeService();
        HttpResponse response = service.handle();

        assertEquals("application/json; charset=utf-8", response.contentType());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("serverTime"));
    }
}
