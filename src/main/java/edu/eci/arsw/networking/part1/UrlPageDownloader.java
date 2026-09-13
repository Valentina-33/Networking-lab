package edu.eci.arsw.networking.part1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Preliminary exercise (networking guide, Section 3.2, Exercise 2) — not
 * part of the graded Part 2 deliverable, kept here as evidence that the
 * prerequisite material was completed before Part 2 began.
 *
 * <p>Asks the user for a URL, reads the page it points to, and stores it in
 * a file named {@code result.html} in the current working directory.</p>
 */
public final class UrlPageDownloader {

    private static final String OUTPUT_FILE = "result.html";

    public static void main(String[] args) throws IOException {
        String urlText;
        try (Scanner keyboard = new Scanner(System.in)) {
            System.out.print("Enter a URL to download: ");
            urlText = keyboard.nextLine().trim();
        }

        URL url;
        try {
            url = URI.create(urlText).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            System.err.println("Not a valid URL: " + urlText);
            return;
        }

        int bytesWritten = 0;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(Path.of(OUTPUT_FILE).toFile(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
                bytesWritten += line.length();
            }
        }

        System.out.println("Saved " + bytesWritten + " characters to " + Path.of(OUTPUT_FILE).toAbsolutePath());
    }
}
