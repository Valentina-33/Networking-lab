package edu.eci.arsw.networking;

import edu.eci.arsw.networking.http.HttpMethod;
import edu.eci.arsw.networking.http.HttpRequest;
import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.services.GreetingService;
import edu.eci.arsw.networking.services.HealthService;
import edu.eci.arsw.networking.services.ServerTimeService;
import edu.eci.arsw.networking.services.SlowService;
import edu.eci.arsw.networking.services.SquareService;

/**
 * Selects a response for an incoming request using direct, explicit
 * conditions on the path. This is deliberately not a general routing
 * framework: the lab wants the path-to-behavior mechanism visible, not
 * hidden behind reflection, annotations, or dependency injection.
 */
public final class RequestRouter {

    private static final String GREETING_PATH = "/api/greeting";
    private static final String SQUARE_PATH = "/api/square";
    private static final String TIME_PATH = "/api/time";
    private static final String HEALTH_PATH = "/api/health";
    private static final String SLOW_PATH = "/api/slow";

    private final GreetingService greetingService = new GreetingService();
    private final SquareService squareService = new SquareService();
    private final ServerTimeService serverTimeService = new ServerTimeService();
    private final HealthService healthService = new HealthService();
    private final SlowService slowService = new SlowService();
    private final StaticResourceHandler staticResourceHandler = new StaticResourceHandler();

    public HttpResponse route(HttpRequest request) {
        if (request.method() != HttpMethod.GET) {
            return HttpResponse.methodNotAllowed(request.rawMethod());
        }

        String path = request.path();

        if (path.equals(GREETING_PATH)) {
            return greetingService.handle(request.queryParams());
        }
        if (path.equals(SQUARE_PATH)) {
            return squareService.handle(request.queryParams());
        }
        if (path.equals(TIME_PATH)) {
            return serverTimeService.handle();
        }
        if (path.equals(HEALTH_PATH)) {
            return healthService.handle();
        }
        if (path.equals(SLOW_PATH)) {
            // Not part of the lab's required four services -- see SlowService.
            return slowService.handle(request.queryParams());
        }

        return staticResourceHandler.handle(path);
    }
}
