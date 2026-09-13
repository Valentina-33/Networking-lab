package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.util.JsonUtil;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Not one of the four required hardcoded services (Section 4 of the lab).
 * This one exists only to make Section 6.2 reproducible with nothing but two
 * browser windows: it blocks for a chosen number of seconds before
 * responding, so a second window's request can be seen sitting behind it
 * while the sequential server works through this one first. Capped at 30
 * seconds so a mistaken value cannot block the single server thread
 * indefinitely.
 */
public final class SlowService {

    private static final int DEFAULT_SECONDS = 5;
    private static final int MAX_SECONDS = 30;

    public HttpResponse handle(Map<String, String> queryParams) {
        String raw = queryParams.get("seconds");
        int seconds = DEFAULT_SECONDS;

        if (raw != null && !raw.isBlank()) {
            try {
                seconds = Integer.parseInt(raw.trim());
            } catch (NumberFormatException e) {
                return HttpResponse.badRequest("{\"error\":\"Query parameter 'seconds' must be an integer: "
                        + JsonUtil.escape(raw) + "\"}");
            }
        }

        if (seconds < 0 || seconds > MAX_SECONDS) {
            return HttpResponse.badRequest("{\"error\":\"Query parameter 'seconds' must be between 0 and "
                    + MAX_SECONDS + "\"}");
        }

        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String finishedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
        String json = "{\"sleptSeconds\":" + seconds + ",\"finishedAt\":" + JsonUtil.quote(finishedAt) + "}";
        return HttpResponse.json(200, "OK", json);
    }
}
