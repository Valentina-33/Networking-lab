package edu.eci.arsw.networking;

import edu.eci.arsw.networking.http.HttpResponse;
import edu.eci.arsw.networking.util.ContentTypes;
import edu.eci.arsw.networking.util.PathNormalizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Serves the static HTML/JS/image resources bundled under
 * {@code src/main/resources/public} (and therefore inside the packaged jar,
 * so the whole application ships as one artifact). The requested path is
 * normalized first so a request can never read a file outside the public
 * resources area.
 */
public final class StaticResourceHandler {

    private static final String PUBLIC_ROOT = "public";
    private static final String INDEX_RESOURCE = "index.html";

    public HttpResponse handle(String requestPath) {
        Optional<String> normalized = PathNormalizer.normalize(requestPath);
        if (normalized.isEmpty()) {
            return HttpResponse.badRequestPlain("400 Bad Request: unsafe path " + requestPath);
        }

        String relative = normalized.get().isEmpty() ? INDEX_RESOURCE : normalized.get();
        String resourceName = PUBLIC_ROOT + "/" + relative;

        byte[] bytes = readResource(resourceName);
        if (bytes == null) {
            return HttpResponse.notFound(requestPath);
        }
        return HttpResponse.ok(bytes, ContentTypes.resolve(relative));
    }

    private byte[] readResource(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resourceName)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
