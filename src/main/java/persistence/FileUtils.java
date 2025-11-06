package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Utilitaria para operaciones de lectura y escritura de archivos.
 * Proporciona métodos estáticos para leer y escribir archivos de texto.
 * @author Wilson Palma
 */

public final class FileUtils {

    // prevenir instanciación
    private FileUtils() {}

    /**
     * Lee el contenido completo del archivo como una cadena.
     * Retorna cadena vacía en caso de error.
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
     * Lee todas las líneas del archivo y las devuelve como una lista de cadenas.
     * Retorna lista vacía en caso de error.
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
     * Escribe texto en archivo (UTF-8). Sobrescribe si el archivo ya existe.
     * Retorna true en caso de éxito.
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
     * Escribe texto al final del archivo (UTF-8). Crea el archivo si no existe.
     * Retorna true en caso de éxito.
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