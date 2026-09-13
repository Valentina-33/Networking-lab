package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.util.JsonUtil;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hardcoded service behind {@code /api/time}: reports the server's own
 * clock, so the browser can see it is talking to a remote process rather
 * than reading its own local clock.
 */
public final class ServerTimeService {

    private final Clock clock;

    public ServerTimeService() {
        this(Clock.systemDefaultZone());
    }

    public ServerTimeService(Clock clock) {
        this.clock = clock;
    }

    public HttpResponse handle() {
        String iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(clock));
        String json = "{\"serverTime\":" + JsonUtil.quote(iso) + "}";
        return HttpResponse.json(200, "OK", json);
    }
}
