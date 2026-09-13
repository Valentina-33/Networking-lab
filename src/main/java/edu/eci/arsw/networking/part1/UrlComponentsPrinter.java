package edu.eci.arsw.networking.part1;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Preliminary exercise (networking guide, Section 3.1, Exercise 1) — not
 * part of the graded Part 2 deliverable, kept here as evidence that the
 * prerequisite material was completed before Part 2 began.
 *
 * <p>Creates a {@link URL} and prints the eight values the guide asks for:
 * protocol, authority, host, port, path, query, file, and ref.</p>
 */
public final class UrlComponentsPrinter {

    private static final String DEFAULT_URL = "http://ldbn.escuelaing.edu.co:80/index.html?lang=en#top";

    public static void main(String[] args) throws MalformedURLException {
        String urlText = args.length > 0 ? args[0] : DEFAULT_URL;
        URL url = new URL(urlText);

        System.out.println("URL: " + urlText);
        System.out.println("Protocol : " + url.getProtocol());
        System.out.println("Authority: " + url.getAuthority());
        System.out.println("Host     : " + url.getHost());
        System.out.println("Port     : " + url.getPort());
        System.out.println("Path     : " + url.getPath());
        System.out.println("Query    : " + url.getQuery());
        System.out.println("File     : " + url.getFile());
        System.out.println("Ref      : " + url.getRef());
    }
}
