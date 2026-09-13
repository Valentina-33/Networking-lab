package edu.eci.arsw.networking.services;

import edu.eci.arsw.networking.http.HttpResponse;

/**
 * Hardcoded service behind {@code /api/health}: a minimal successful
 * response used to verify the process is up and serving requests, both
 * locally and once deployed on EC2.
 */
public final class HealthService {

    public HttpResponse handle() {
        return HttpResponse.json(200, "OK", "{\"status\":\"UP\"}");
    }
}
