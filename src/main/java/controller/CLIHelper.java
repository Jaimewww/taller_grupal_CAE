package controller;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * CLIHelper
 *
 * Helper simple para interacción en consola:
 * - Mensajes coloreados y con símbolos según convención:
 *   !(warning)!  -> amarillo
 *   !!(error)!!  -> rojo
 *   ?(info)?     -> azul
 *   ✓(success)✓  -> verde
 *
 * - Métodos de entrada:
 *   String ask(String prompt)
 *   int askOption(String prompt, int min, int max)
 *
 * Nota: usa ANSI colors; en terminales que no soporten ANSI los códigos aparecerán tal cual.
 * Si necesitás quitar los colores, asigná ENABLE_COLOR = false.
 * @author Wilson Palma
 */
public class CLIHelper implements AutoCloseable {

    private final Scanner scanner;
    private final boolean ENABLE_COLOR;

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String BOLD = "\u001B[1m";

    public CLIHelper() {
        this(true);
    }

    /**
     * Constructor.
     * @param enableColor habilita/deshabilita códigos ANSI (útil en entornos Windows sin soporte ANSI)
     */
    public CLIHelper(boolean enableColor) {
        this.scanner = new Scanner(System.in);
        this.ENABLE_COLOR = enableColor;
    }

    // -------------------- Mensajes (uso los símbolos solicitados) --------------------

    public void printInfo(String msg) {
        String tag = "?(info)?";
        if (ENABLE_COLOR) {
            System.out.println(BLUE + tag + " " + msg + RESET);
        } else {
            System.out.println(tag + " " + msg);
        }
    }

    public void printSuccess(String msg) {
        String tag = "✓(success)✓";
        if (ENABLE_COLOR) {
            System.out.println(GREEN + tag + " " + msg + RESET);
        } else {
            System.out.println(tag + " " + msg);
        }
    }

    public void printAlert(String msg) {
        String tag = "!(warning)!";
        if (ENABLE_COLOR) {
            System.out.println(YELLOW + tag + " " + msg + RESET);
        } else {
            System.out.println(tag + " " + msg);
        }
    }

    public void printError(String msg) {
        String tag = "!!(error)!!";
        if (ENABLE_COLOR) {
            System.err.println(RED + tag + " " + msg + RESET);
        } else {
            System.err.println(tag + " " + msg);
        }
    }

    // -------------------- Prompts / input --------------------

    /**
     * ask - pide una línea de texto al usuario. Reintenta si la entrada es EOF.
     * @param prompt mensaje que se muestra
     * @return la línea ingresada (trim)
     */
    public String ask(String prompt) {
        try {
            System.out.print(prompt + " ");
            String line = scanner.nextLine();
            if (line == null) return "";
            return line.trim();
        } catch (NoSuchElementException | IllegalStateException ex) {
            // Scanner cerrado o EOF; devolver cadena vacía y registrar alerta
            printAlert("Entrada no disponible: " + ex.getMessage());
            return "";
        }
    }

    /**
     * askOption - pide una opción numérica entre min y max (incluyentes).
     * Reintenta hasta que el usuario ingrese un entero válido.
     *
     * @param prompt el texto a mostrar (se añadirá " (min-max):")
     * @param min valor mínimo aceptado (inclusive)
     * @param max valor máximo aceptado (inclusive)
     * @return la opción elegida
     */
    public int askOption(String prompt, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min no puede ser mayor que max");
        while (true) {
            try {
                System.out.print(String.format("%s (%d-%d): ", prompt, min, max));
                String line = scanner.nextLine();
                if (line == null) {
                    printAlert("Entrada no disponible.");
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    printAlert("¡Por favor ingresa una opción válida!");
                    continue;
                }
                int opt = Integer.parseInt(line);
                if (opt < min || opt > max) {
                    printAlert("Por favor selecciona un número entre " + min + " y " + max + ".");
                    continue;
                }
                return opt;
            } catch (NumberFormatException nfe) {
                printAlert("Entrada inválida. Ingresa un número entero.");
            } catch (NoSuchElementException | IllegalStateException ex) {
                printAlert("Entrada no disponible: " + ex.getMessage());
                return min; // fallback razonable
            }
        }
    }

    /**
     * askYesNo - pregunta sí/no (Y/N). Retorna true si usuario responde Y/y.
     * @param prompt mensaje
     * @return boolean
     */
    public boolean askYesNo(String prompt) {
        while (true) {
            String resp = ask(prompt + " (Y/N):");
            if (resp == null) return false;
            resp = resp.trim();
            if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes")) return true;
            if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no")) return false;
            printAlert("Respuesta inválida. Ingresa Y o N.");
        }
    }

    /** Cierra recursos (scanner). */
    @Override
    public void close() {
        try {
            scanner.close();
        } catch (Exception ignored) {}
    }
}
