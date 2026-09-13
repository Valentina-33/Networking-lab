package edu.eci.arsw.networking;

import edu.eci.arsw.networking.http.HttpMethod;
import edu.eci.arsw.networking.http.HttpRequest;
import edu.eci.arsw.networking.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestRouterTest {

    private final RequestRouter router = new RequestRouter();

    private HttpRequest get(String path, Map<String, String> query) {
        return new HttpRequest(HttpMethod.GET, "GET", path, query, "HTTP/1.1");
    }

    @Test
    void nonGetMethodIsAlwaysMethodNotAllowedRegardlessOfPath() {
        HttpRequest request = new HttpRequest(HttpMethod.OTHER, "DELETE", "/api/health", Map.of(), "HTTP/1.1");

        HttpResponse response = router.route(request);

        assertEquals(405, response.statusCode());
    }

    @Test
    void methodNotAllowedAdvertisesTheAcceptedMethodViaAllowHeader() {
        HttpRequest request = new HttpRequest(HttpMethod.OTHER, "POST", "/api/greeting", Map.of(), "HTTP/1.1");

        HttpResponse response = router.route(request);

        assertEquals("GET", response.headers().get("Allow"));
    }

    @Test
    void routesToGreetingService() {
        HttpResponse response = router.route(get("/api/greeting", Map.of("name", "Ada")));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("Ada"));
    }

    @Test
    void routesToSquareService() {
        HttpResponse response = router.route(get("/api/square", Map.of("value", "4")));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("16"));
    }

    @Test
    void routesToServerTimeService() {
        HttpResponse response = router.route(get("/api/time", Map.of()));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("serverTime"));
    }

    @Test
    void routesToHealthService() {
        HttpResponse response = router.route(get("/api/health", Map.of()));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("UP"));
    }

    @Test
    void routesToSlowService() {
        // seconds=0 keeps this test fast; SlowServiceTest covers the timing behavior.
        HttpResponse response = router.route(get("/api/slow", Map.of("seconds", "0")));

        assertEquals(200, response.statusCode());
        assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("sleptSeconds"));
    }

    @Test
    void unrecognizedPathFallsThroughToStaticResourceHandler() {
        // index.html is bundled under src/main/resources/public, so the root
        // path must resolve to a real 200, not a static-handler 404.
        HttpResponse response = router.route(get("/", Map.of()));

        assertEquals(200, response.statusCode());
        assertEquals("text/html; charset=utf-8", response.contentType());
    }

    @Test
    void missingStaticResourceIsNotFound() {
        HttpResponse response = router.route(get("/does-not-exist.html", Map.of()));

        assertEquals(404, response.statusCode());
    }
}
