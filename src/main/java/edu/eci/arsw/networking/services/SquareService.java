package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.util.JsonUtil;

import java.util.Map;

/**
 * Hardcoded service behind {@code /api/square}: reads a numeric
 * {@code value} query parameter and returns it together with its square.
 */
public final class SquareService {

    public HttpResponse handle(Map<String, String> queryParams) {
        String rawValue = queryParams.get("value");
        if (rawValue == null || rawValue.isBlank()) {
            return HttpResponse.badRequest("{\"error\":\"Missing required query parameter: value\"}");
        }

        double value;
        try {
            value = Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException e) {
            return HttpResponse.badRequest("{\"error\":\"Query parameter 'value' must be numeric: "
                    + JsonUtil.escape(rawValue) + "\"}");
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return HttpResponse.badRequest("{\"error\":\"Query parameter 'value' must be a finite number\"}");
        }

        double square = value * value;
        String json = "{\"input\":" + format(value) + ",\"square\":" + format(square) + "}";
        return HttpResponse.json(200, "OK", json);
    }

    private String format(double d) {
        if (d == Math.rint(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }
}
