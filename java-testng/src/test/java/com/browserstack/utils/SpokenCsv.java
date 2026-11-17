package com.browserstack.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves what the screen reader spoke to a CSV file (step, shortcut, spoken text).
 * Useful for frameworks where BrowserStack's Screen Reader > Automation report is not available:
 * the spoken description can still be captured, reviewed offline and attached to build artifacts.
 */
public final class SpokenCsv {

    private SpokenCsv() {}

    /** Writes target/spoken-output/{name}.csv and returns its path. */
    public static Path write(String name, String shortcut, List<String> spoken) {
        List<String> lines = new ArrayList<>();
        lines.add("step,shortcut,spoken_text");
        for (int i = 0; i < spoken.size(); i++) {
            lines.add((i + 1) + "," + escape(shortcut) + "," + escape(spoken.get(i)));
        }
        try {
            Path dir = Paths.get("target", "spoken-output");
            Files.createDirectories(dir);
            Path file = dir.resolve(name + ".csv");
            Files.write(file, lines, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write spoken-output CSV", e);
        }
    }

    // RFC 4180: quote every field and double any quotes; collapse the repeated spaces NVDA inserts between parts.
    static String escape(String value) {
        String clean = value == null ? "" : value.trim().replaceAll("\\s{2,}", " ");
        return "\"" + clean.replace("\"", "\"\"") + "\"";
    }
}
