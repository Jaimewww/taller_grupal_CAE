package util;

import java.util.Scanner;

public class InputValidator {

    private static final Scanner sc = new Scanner(System.in);

    //Lee un entero cualquiera, repitiendo hasta que el usuario ingrese un número válido.
    public static int getValidatedInt(String prompt) {
        int value;
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                value = Integer.parseInt(line);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingrese un número válido.");
            }
        }
    }

    //Lee un entero en un rango [min, max], repite hasta recibir un valor correcto.
    public static int getIntInRange(String prompt, int min, int max) {
        int value;
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Por favor, ingrese un número válido entre " + min + " y " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingrese un número válido.");
            }
        }
    }

    //Lee un nombre válido: no vacío, solo letras y espacios (acepta acentos y ñ).
    public static String getValidatedName(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("El nombre no puede estar vacío. Intente de nuevo.");
                continue;
            }
            // Permitir letras, espacios y caracteres acentuados y ñ
            if (!input.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
                System.out.println("Nombre inválido. Use solo letras y espacios.");
                continue;
            }
            return input;
        }
    }

    //Lee un string no vacío (para observaciones/ notas). Permite puntuación básica.
    public static String getNonEmptyString(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("El valor no puede estar vacío. Intente de nuevo.");
                continue;
            }
            return input;
        }
    }

    //Confirma con el usuario si o no (s/n).
    public static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s")) return true;
            if (resp.equals("n")) return false;
            System.out.println("Respuesta inválida. Ingrese 's' o 'n'.");
        }
    }
}
