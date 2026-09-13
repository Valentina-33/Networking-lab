package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.util.JsonUtil;

import java.util.Map;

/**
 * Hardcoded service behind {@code /api/greeting}: reads a {@code name} query
 * parameter and returns a JSON greeting that safely embeds it.
 */
public final class GreetingService {

    public HttpResponse handle(Map<String, String> queryParams) {
        String name = queryParams.get("name");
        if (name == null || name.isBlank()) {
            return HttpResponse.badRequest("{\"error\":\"Missing or empty required query parameter: name\"}");
        }

        String message = "Hello, " + name.trim() + "!";
        String json = "{\"name\":" + JsonUtil.quote(name.trim()) + ",\"message\":" + JsonUtil.quote(message) + "}";
        return HttpResponse.json(200, "OK", json);
    }
}
