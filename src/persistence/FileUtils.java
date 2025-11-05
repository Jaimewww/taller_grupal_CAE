package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file utilities for UTF-8 text I/O.
 * Small and easy to use from other classes.
 */
public final class FileUtils {

    // prevent instantiation
    private FileUtils() {}

    /**
     * Read whole file as a string (UTF-8).
     * Returns empty string if file does not exist or an error occurs.
     */
    public static String readFile(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return "";
        }
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file: " + path + " - " + e.getMessage());
            return "";
        }
    }

    /**
     * Read all lines into a list. Returns empty list on error.
     */
    public static List<String> readLines(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading lines from: " + path + " - " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Write text to file (UTF-8). Creates parent folders if needed.
     * Overwrites existing content.
     * Returns true on success.
     */
    public static boolean writeFile(String path, String content) {
        Path p = Paths.get(path);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.writeString(p, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file: " + path + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Append text to file (UTF-8). Creates file if it does not exist.
     * Returns true on success.
     */
    public static boolean appendToFile(String path, String content) {
        Path p = Paths.get(path);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.writeString(p, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error appending to file: " + path + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if file exists.
     */
    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
}