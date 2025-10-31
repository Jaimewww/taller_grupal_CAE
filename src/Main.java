import domine.TicketState;
import domine.Note;
import domine.Ticket;
import domine.ProcedureType;
import estructures.Queue;
import estructures.Stack;
import estructures.SimpleList;
import estructures.Node;
import util.InputValidator;

public class Main {
    static Queue<Ticket> ticketQueue = new Queue<>();
    static Queue<Ticket> finishedCases = new Queue<>();
    static Ticket currentTicket = null;

    public static void main(String[] args) {
        int option;
        do {
            showMenu();
            option = InputValidator.getIntInRange("Seleccione una opción: ", 0, 4);
            handleMenuOption(option);
        } while (option != 0);
        System.out.println("Saliendo del sistema...");
    }

    private static void showMenu() {
        System.out.println("\n*---[ SISTEMA DE GESTIÓN DE CASOS / CAE ]---*");
        System.out.println("*********************************************");
        System.out.println("1. Recibir nuevo caso");
        System.out.println("2. Atender siguiente caso");
        System.out.println("3. Ver casos en espera");
        System.out.println("4. Consultar historial de caso (por ID)");
        System.out.println("0. Salir");
    }

    private static void handleMenuOption(int option) {
        switch (option) {
            case 1:
                receiveNewCase();
                break;
            case 2:
                attendNextCase();
                break;
            case 3:
                viewPendingCases();
                break;
            case 4:
                showHistory();
                break;
            case 0:
                break;
            default:
                System.out.println("Opción inválida. Por favor intente de nuevo.");
        }
    }

    private static void receiveNewCase() {
        try {
            String name = InputValidator.getValidatedName("Nombre del estudiante: ");
            ProcedureType[] types = ProcedureType.values();  // elegir tipo de trámite mediante enum
            System.out.println("Seleccione el tipo de trámite:");
            for (int i = 0; i < types.length; i++) {
                System.out.println((i + 1) + ". " + types[i]);
            }
            int choice = InputValidator.getIntInRange("Opción: ", 1, types.length);
            String procedure = types[choice - 1].name();

            Ticket ticket = new Ticket(name, procedure);
            ticket.setId(ticketQueue.size() + finishedCases.size() + 1);
            ticketQueue.enqueue(ticket);

            System.out.println(">> Caso Nro. [" + ticket.getId() + "] recibido y puesto en cola correctamente <<");
        } catch (Exception e) {
            System.out.println("Error al recibir el caso: " + e.getMessage());
        }
    }

    private static void attendNextCase() {
        if (ticketQueue.isEmpty()) {
            System.out.println("No hay casos en espera");
            return;
        }

        // obtener el siguiente ticket
        Ticket ticket = ticketQueue.dequeue();
        ticket.setState(TicketState.EN_ATENCION);
        currentTicket = ticket;

        // pilas locales por sesión para evitar errores entre tickets
        Stack<Note> undoStack = new Stack<>();
        Stack<Note> redoStack = new Stack<>();

        System.out.println("Atendiendo caso ID: [" + ticket.getId() + "] - " + ticket.getStudent());

        int option;
        do {
            System.out.println("\n1. Agregar nota");
            System.out.println("2. Deshacer última nota");
            System.out.println("3. Rehacer nota");
            System.out.println("4. Ver historial de notas");
            System.out.println("5. Finalizar caso");
            option = InputValidator.getIntInRange("Seleccione una opción: ", 1, 5);

            try {
                switch (option) {
                    case 1:
                        if (ticket.getState() == TicketState.COMPLETADO) {
                            System.out.println("El caso ya está finalizado. NO se puede agregar notas.");
                            break;
                        }
                        String obs = InputValidator.getNonEmptyString("Ingrese la observación: ");
                        Note note = ticket.agregarNota(obs);
                        undoStack.push(note);
                        redoStack = new Stack<>(); //una nueva acción invalida la posibilidad de rehacer anteriores
                        System.out.println("Nota agregada correctamente -- " + note);
                        break;

                    case 2:
                        if (undoStack.isEmpty()) {
                            System.out.println("No hay notas por deshacer.");
                        } else if (ticket.getState() == TicketState.COMPLETADO) {
                            System.out.println("No se puede deshacer un caso finalizado.");
                        } else {
                            Note last = undoStack.pop();
                            try {
                                ticket.getNoteHistory().remove(last);
                                redoStack.push(last);
                                System.out.println("Última nota deshecha correctamente -- " + last);
                            } catch (Exception e) {
                                System.out.println("No se pudo eliminar la nota (posible inconsistencia).");
                            }
                        }
                        break;

                    case 3:
                        if (redoStack.isEmpty()) {
                            System.out.println("No hay notas por rehacer.");
                        } else if (ticket.getState() == TicketState.COMPLETADO) {
                            System.out.println("No se puede rehacer en un caso finalizado.");
                        } else {
                            Note redo = redoStack.pop();
                            ticket.getNoteHistory().pushBack(redo);
                            undoStack.push(redo);
                            System.out.println("Nota rehecha correctamente -- " + redo);
                        }
                        break;

                    case 4:
                        showNotesOfTicket(ticket);
                        break;

                    case 5:
                        ticket.setState(TicketState.COMPLETADO);
                        finishedCases.enqueue(ticket);
                        // limpiar pilas (seguro)
                        while (!undoStack.isEmpty()) {
                            try { undoStack.pop(); } catch (Exception ignored) {}
                        }
                        while (!redoStack.isEmpty()) {
                            try { redoStack.pop(); } catch (Exception ignored) {}
                        }
                        System.out.println("Caso Nro. [" + ticket.getId() + "] finalizado.");
                        break;

                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("Ocurrió un error durante la atención: " + e.getMessage());
            }

        } while (option != 5);

        currentTicket = null;
    }

    private static void showNotesOfTicket(Ticket ticket) {
        System.out.println("\nNotas registradas para el caso " + ticket.getId() + ":");
        SimpleList<Note> notes = ticket.getNoteHistory();
        if (notes.isEmpty()) {
            System.out.println("No hay notas registradas");
            return;
        }
        Node<Note> current = notes.head;
        while (current != null) {
            System.out.println("- " + current.value);
            current = current.next;
        }
    }

    private static void viewPendingCases() {
        System.out.println("\nCasos en espera: " + ticketQueue.size());
        if (ticketQueue.isEmpty()) {
            System.out.println("No hay casos en espera");
            return;
        }

        Queue<Ticket> temp = new Queue<>();
        try {
            while (!ticketQueue.isEmpty()) {
                Ticket t = ticketQueue.dequeue();
                System.out.println("ID: [" + t.getId() + "] --- Nombre: [" + t.getStudent() + "] --- Tipo de trámite: [" + t.getProcedureType() + "]");
                temp.enqueue(t);
            }
        } catch (Exception e) {
            System.out.println("Error al listar casos: " + e.getMessage());
        } finally {
            while (!temp.isEmpty()) ticketQueue.enqueue(temp.dequeue());
        }
    }

    private static void showHistory() {
        int id = InputValidator.getValidatedInt("Ingrese ID del caso: ");
        Ticket found = findTicketById(id);

        if (found == null) {
            System.out.println("No se encontró el caso con ID [" + id + "]");
            return;
        }

        System.out.println("\nHistorial del caso " + found.getId());
        System.out.println("Estudiante: " + found.getStudent());
        System.out.println("Trámite: " + found.getProcedureType());
        System.out.println("Estado: " + found.getState());
        showNotesOfTicket(found);
    }

    private static Ticket findTicketById(int id) {
        // buscar en currentTicket
        if (currentTicket != null && currentTicket.getId() == id) return currentTicket;

        Ticket found = null;
        Queue<Ticket> tempQueue = new Queue<>();
        while (!ticketQueue.isEmpty()) {
            Ticket t = ticketQueue.dequeue();
            if (t.getId() == id) found = t;
            tempQueue.enqueue(t);
        }
        while (!tempQueue.isEmpty()) ticketQueue.enqueue(tempQueue.dequeue());

        Queue<Ticket> tempFinished = new Queue<>();
        while (!finishedCases.isEmpty()) {
            Ticket t = finishedCases.dequeue();
            if (t.getId() == id) found = t;
            tempFinished.enqueue(t);
        }
        while (!tempFinished.isEmpty()) finishedCases.enqueue(tempFinished.dequeue());

        return found;
    }
}
