import dominio.EstadoTicket;
import dominio.Nota;
import dominio.Ticket;
import estructuras.Queue;
import estructuras.Stack;
import estructuras.SimpleList;
import estructuras.Node;

import java.util.Scanner;

public class Main {

    static Queue<Ticket> ticketQueue = new Queue<>();
    static Queue<Ticket> finishedCases = new Queue<>();
    static Stack<Nota> undoStack = new Stack<>();
    static Stack<Nota> redoStack = new Stack<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int option;
        do {
            showMenu();
            option = getIntInput();
            handleMenuOption(option);
        } while (option != 0);
    }

    private static void showMenu() {
        System.out.println("\n*---[ SISTEMA DE GESTIÓN DE CASOS / CAE ]---*");
        System.out.println("*********************************************");
        System.out.println("1. Recibir nuevo caso");
        System.out.println("2. Atender siguiente caso");
        System.out.println("3. Ver casos en espera");
        System.out.println("4. Consultar historial de caso (por ID)");
        System.out.println("0. Salir");
        System.out.print("\nSeleccione una opción: ");
    }

    private static int getIntInput() {
        int value = -1;
        try {
            value = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Por favor ingrese un número válido");
        }
        return value;
    }

    private static void handleMenuOption(int option) {
        switch (option) {
            case 1:
                recibirNuevoCaso();
                break;
            case 2:
                atenderSiguienteCaso();
                break;
            case 3:
                verCasosEnEspera();
                break;
            case 4:
                consultarHistorial();
                break;
            case 0:
                System.out.println("Saliendo del sistema...");
                break;
            default:
                System.out.println("Opción inválida. Por favor intente de nuevo");
        }
    }

    private static void recibirNuevoCaso() {
        System.out.print("Nombre del estudiante: ");
        String nombre = sc.nextLine();
        System.out.print("Tipo de trámite: ");
        String tramite = sc.nextLine();

        Ticket ticket = new Ticket(nombre, tramite);
        ticket.setId(ticketQueue.size() + finishedCases.size() + 1);
        ticketQueue.enqueue(ticket);

        System.out.println(">> Caso Nro. [" + ticket.getId() + "] recibido y puesto en cola correctamente <<");
    }

    private static void atenderSiguienteCaso() {
        if (ticketQueue.isEmpty()) {
            System.out.println("No hay casos en espera");
            return;
        }

        Ticket ticket = ticketQueue.dequeue();
        ticket.setEstado(EstadoTicket.EN_ATENCION);
        System.out.println("Atendiendo caso ID: [" + ticket.getId() + "] a nombre de: [" + ticket.getEstudiante()+"]");

        int option;
        do {
            System.out.println("\n1. Agregar nota");
            System.out.println("2. Deshacer última nota");
            System.out.println("3. Rehacer nota");
            System.out.println("4. Finalizar caso");
            System.out.print("Seleccione una opción: ");
            option = getIntInput();

            switch (option) {
                case 1:
                    System.out.print("Ingrese la observación: ");
                    String obs = sc.nextLine();
                    Nota nota = ticket.agregarNota(obs);
                    undoStack.push(nota);
                    redoStack = new Stack<>();
                    System.out.println("Nota agregada correctamente");
                    break;
                case 2:
                    if (undoStack.isEmpty()) {
                        System.out.println("No hay notas por deshacer");
                    } else {
                        Nota last = undoStack.pop();
                        ticket.getHistorialNotas().remove(last);
                        redoStack.push(last);
                        System.out.println("Última nota deshecha correctamente");
                    }
                    break;
                case 3:
                    if (redoStack.isEmpty()) {
                        System.out.println("No hay notas por rehacer");
                    } else {
                        Nota redo = redoStack.pop();
                        ticket.getHistorialNotas().pushBack(redo);
                        undoStack.push(redo);
                        System.out.println("Nota rehecha correctamente");
                    }
                    break;
                case 4:
                    ticket.setEstado(EstadoTicket.COMPLETADO);
                    finishedCases.enqueue(ticket);
                    System.out.println("Caso Nro. [" + ticket.getId() + "] finalizado");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        } while (option != 4);
    }

    private static void verCasosEnEspera() {
        System.out.println("\nCasos en espera: " + ticketQueue.size());
        if (ticketQueue.isEmpty()) {
            System.out.println("No hay casos en espera");
            return;
        }

        Queue<Ticket> temp = new Queue<>();
        while (!ticketQueue.isEmpty()) {
            Ticket t = ticketQueue.dequeue();
            System.out.println("ID: ["+ t.getId() +"] --- Nombre: ["+ t.getEstudiante() +"] --- Tipo de trámite: ["+t.getTipoTramite()+"]");
            temp.enqueue(t);
        }

        while (!temp.isEmpty()) ticketQueue.enqueue(temp.dequeue());
    }

    private static void consultarHistorial() {
        System.out.print("Ingrese ID del caso: ");
        int id = getIntInput();
        Ticket found = null;

        // Buscar en cola
        Queue<Ticket> tempQueue = new Queue<>();
        while (!ticketQueue.isEmpty()) {
            Ticket t = ticketQueue.dequeue();
            if (t.getId() == id) found = t;
            tempQueue.enqueue(t);
        }
        while (!tempQueue.isEmpty()) ticketQueue.enqueue(tempQueue.dequeue());

        // Buscar en casos finalizados si no se encontró
        if (found == null) {
            Queue<Ticket> tempFinished = new Queue<>();
            while (!finishedCases.isEmpty()) {
                Ticket t = finishedCases.dequeue();
                if (t.getId() == id) found = t;
                tempFinished.enqueue(t);
            }
            while (!tempFinished.isEmpty()) finishedCases.enqueue(tempFinished.dequeue());
        }

        if (found == null) {
            System.out.println("No se encontró el caso con ID [" +id+"]");
            return;
        }

        System.out.println("\nHistorial del caso " + found.getId());
        System.out.println("Estudiante: " + found.getEstudiante());
        System.out.println("Trámite: " + found.getTipoTramite());
        System.out.println("Estado: " + found.getEstado());
        System.out.println("Notas:");

        SimpleList<Nota> notas = found.getHistorialNotas();
        if (notas.isEmpty()) {
            System.out.println("No hay notas registradas");
        } else {
            Node<Nota> current = notas.head;
            while (current != null) {
                System.out.println("- " + current.value);
                current = current.next;
            }
        }
    }
}
