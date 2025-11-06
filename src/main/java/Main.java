// Main.java
import java.util.*;
import java.time.format.DateTimeFormatter;

import domine.*;
import estructures.*;
import persistence.*;
import reports.*;
import util.*;
import controller.*;


/*
* Esta clase Main implementa una interfaz de línea de comandos (CLI) para un sistema de gestión de tickets de atención.
* Permite a los usuarios crear y gestionar tickets, atenderlos, cambiar su estado, deshacer/rehacer acciones y generar reportes.
* Utiliza varias clases auxiliares para manejar la lógica del sistema, la persistencia de datos y la generación de reportes.
* @Author Jaime Landázuri, Alejandro Padilla, Cael Soto, Wilson Palma
*
* */

public class Main {
    
    // Helper para interacción CLI (impresión de mensajes formateados)
    static final CLIHelper cliHelper = new CLIHelper();

    private static final Scanner scanner = new Scanner(System.in);
    private static CaeController controller;

    // Punto de entrada: inicializa componentes y ejecuta el bucle CLI
    public static void main(String[] args) {
        try {

            // Atención / colas
            AttentionQueue attentionQueue = new AttentionQueue();

            // Pila de acciones (undo/redo)
            ActionStack actionStack = new ActionStack();

            // Persistencia
            PersistenceManager persistenceManager;
            try {
                persistenceManager = new PersistenceManager();
            } catch (Throwable t) {
                // fallback minimal si no existe constructor por defecto
                persistenceManager = null;
                cliHelper.printInfo("Aviso: PersistenceManager no pudo instanciarse por defecto. Ajusta el Main.java. >> " + t.getMessage());
            }

            // Reportes
            ReportManager reportManager;
            try {
                reportManager = new ReportManager();
            } catch (Throwable t) {
                reportManager = null;
                cliHelper.printError("Aviso: ReportManager no pudo instanciarse por defecto. Ajusta el Main.java. >> " + t.getMessage());
            }

            // State machine
            StateMachine stateMachine = new StateMachine();

            // Reloj del sistema (abstracción)
            SystemClock clock = new SystemClock();


            // Construye el controlador con todas las dependencias necesarias
            controller = new CaeController(
                    attentionQueue,
                    actionStack,
                    persistenceManager,
                    reportManager,
                    stateMachine,
                    clock,
                    cliHelper
            );

            // Intentar carga/inicialización si existe start()
            try {
                controller.start();
            } catch (NoSuchMethodError | AbstractMethodError e) {
                // ignora si no existe
            } catch (Exception ex) {
                cliHelper.printError("Aviso: error al invocar start() del controlador: " + ex.getMessage());
            }

            // Bucle principal del CLI: muestra menú y procesa opciones del usuario
            boolean running = true;
            while (running) {
                printHeader();
                printMenu();
                int option = askInt("Seleccione una opción: ", 1, 9);
                switch (option) {
                    case 1:
                        opcionCrearTicket();
                        break;
                    case 2:
                        opcionAtenderSiguiente();
                        break;
                    case 3:
                        opcionListarPendientes();
                        break;
                    case 4:
                        opcionConsultarHistorial();
                        break;
                    case 5:
                        opcionCambiarEstado();
                        break;
                    case 6:
                        opcionUndo();
                        break;
                    case 7:
                        opcionRedo();
                        break;
                    case 8:
                        opcionGenerarReporte();
                        break;
                    case 9:
                        running = false;
                        break;
                    default:
                        cliHelper.printError("!(Opcion invalida)!");
                }
            }

            // Guardar / cerrar
            try {
                controller.shutdown();
            } catch (NoSuchMethodError | AbstractMethodError e) {
                // ignore
            } catch (Exception ex) {
                cliHelper.printError("Error al guardar/cerrar: " + ex.getMessage());
            }

            cliHelper.printSuccess("✓(Operación finalizada)✓ - Saliendo...");

        } catch (Throwable t) {
            cliHelper.printError("Error crítico en la inicialización: " + t.getMessage());
            cliHelper.printError("Detalle: " + t.toString());
        } finally {
            scanner.close();
        }
    }


    // Menú y acciones
    private static void printHeader() {
        System.out.println("======================================");
        System.out.println("   Sistema de Atención de Tickets");
        System.out.println("======================================");
    }

    private static void printMenu() {
        System.out.println("1) Nuevo ticket");
        System.out.println("2) Atender siguiente");
        System.out.println("3) Listar tickets pendientes");
        System.out.println("4) Consultar historial (completados)");
        System.out.println("5) Cambiar estado de ticket");
        System.out.println("6) Deshacer (undo)");
        System.out.println("7) Rehacer (redo)");
        System.out.println("8) Generar reportes");
        System.out.println("9) Salir y guardar");
    }

    // Opción 1: crear ticket interactivo
    private static void opcionCrearTicket() {
        System.out.println("-- Crear nuevo ticket --");
        String student = askString("Nombre del estudiante: ");
        ProcedureType type = askProcedureType();
        boolean urgent = askYesNo("¿Prioridad urgente? (s/n): ");
        try {
            Ticket t = controller.createTicket(student, type, urgent);
            cliHelper.printSuccess("✓(Ticket creado)✓ ID: " + t.getId() + " - " + t.getStudent() + " (" + t.getProcedureType() + ")");
        } catch (Exception e) {
            cliHelper.printError("Error al crear ticket: " + e.getMessage());
        }
    }

    // Opción 2: atender siguiente ticket de la cola
    private static void opcionAtenderSiguiente() {
        System.out.println("-- Atender siguiente --");
        try {
            Ticket t = controller.attendNext();
            if (t == null) {
                cliHelper.printInfo("?(info)? No hay tickets por atender.");
                return;
            }
            System.out.println("Atendiendo ticket ID: " + t.getId() + " - " + t.getStudent() + " [" + t.getProcedureType() + "]");
            boolean atendiendo = true;
            while (atendiendo) {
                System.out.println("Acciones sobre ticket:");
                System.out.println(" 1) Agregar nota");
                System.out.println(" 2) Marcar PENDIENTE_DOCS (volver a la cola)");
                System.out.println(" 3) Finalizar (COMPLETADO)");
                System.out.println(" 4) Volver al menú principal");
                int a = askInt("Elija acción: ", 1, 4);
                switch (a) {
                    case 1:
                        String obs = askString("Observación: ");
                        try {
                            Note n = controller.addNoteToTicket(t, obs);
                            cliHelper.printSuccess("✓(Nota añadida)✓ " + n.toString());
                        } catch (Exception ex) {
                            cliHelper.printError("Error al añadir nota: " + ex.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            controller.changeTicketState(t.getId(), TicketState.PENDIENTE_DOCS);
                            cliHelper.printSuccess("✓(Estado cambiado)✓ -> PENDIENTE_DOCS");
                        } catch (Exception ex) {
                            cliHelper.printError("No se pudo cambiar estado: " + ex.getMessage());
                        }
                        atendiendo = false;
                        break;
                    case 3:
                        try {
                            controller.finalizeTicket(t);
                            cliHelper.printSuccess("✓(Ticket finalizado)✓ -> COMPLETADO");
                        } catch (Exception ex) {
                            cliHelper.printError("Error al finalizar: " + ex.getMessage());
                        }
                        atendiendo = false;
                        break;
                    case 4:
                        t.setState(TicketState.EN_COLA);
                        atendiendo = false;
                        break;
                }
            }

        } catch (NoSuchElementException ne) {
            cliHelper.printInfo("?(info)? No hay tickets por atender.");
        } catch (Exception e) {
            cliHelper.printError("Error en atender siguiente: " + e.getMessage());
        }
    }

    // Opción 3: listar tickets pendientes (snapshot)
    private static void opcionListarPendientes() {
        System.out.println("-- Tickets pendientes (snapshot) --");
        try {
            List<Ticket> pendientes = controller.listPending();
            if (pendientes == null || pendientes.isEmpty()) {
                cliHelper.printInfo("?(info)? No hay tickets pendientes.");
                return;
            }
            for (Ticket t : pendientes) {
                System.out.println(formatTicketLine(t));
            }
        } catch (Exception e) {
            cliHelper.printError("Error al listar pendientes: " + e.getMessage());
        }
    }

    // Opción 4: mostrar historial de atendidos
    private static void opcionConsultarHistorial() {
        System.out.println("-- Historial (completados) --");
        try {
            SimpleList<Ticket> history = controller.getAttentionQueue().getAttendedHistory();
            if (history == null || history.isEmpty()) {
                cliHelper.printInfo("?(info)? No hay historial registrado.");
                return;
            }
            Node<Ticket> cursor = history.head;
            while (cursor != null) {
                System.out.println(formatTicketLine(cursor.value));
                cursor = cursor.next;
            }
        } catch (Exception e) {
            cliHelper.printError("Error al consultar historial: " + e.getMessage());
        }
    }

    // Opción 5: cambiar estado de ticket (interactivo)
    private static void opcionCambiarEstado() {
        System.out.println("-- Cambiar estado de ticket --");
        int id = askInt("ID del ticket: ", 1, Integer.MAX_VALUE);
        try {
            Ticket t = controller.findTicketById(id);
            if (t == null) {
                cliHelper.printError("!(error)! Ticket no encontrado.");
                return;
            }
            System.out.println("Ticket: " + formatTicketLine(t));
            try {
                List<TicketState> allowed = controller.getStateMachine().allowedNextStates(t.getState());
                System.out.println("Estados válidos desde " + t.getState() + ":");
                for (int i = 0; i < allowed.size(); i++) {
                    System.out.println("  " + (i+1) + ") " + allowed.get(i));
                }
                int sel = askInt("Seleccione nuevo estado: ", 1, allowed.size());
                TicketState target = allowed.get(sel-1);
                controller.changeTicketState(id, target);
                cliHelper.printSuccess("✓(Estado cambiado)✓ -> " + target);
            } catch (Exception ex) {
                cliHelper.printError("No se pudo consultar StateMachine; seleccione estado manualmente:" + ex.getMessage());
                TicketState target = askTicketState();
                controller.changeTicketState(id, target);
                cliHelper.printSuccess("✓(Estado cambiado)✓ -> " + target);
            }
        } catch (Exception e) {
            cliHelper.printError("Error al cambiar estado: " + e.getMessage());
        }
    }

    // Opción 6: deshacer última acción (undo)
    private static void opcionUndo() {
        try {
            if(!controller.getActionStack().getUndoStack().isEmpty()){
                IAction accion = controller.getActionStack().getUndoStack().peek();
                controller.undo();
                cliHelper.printSuccess("✓(Deshacer)✓ Acción deshecha: " + accion.toString());
                return;
            }
            cliHelper.printAlert("!(No hay acciones por deshacer)!");

        } catch (Exception e) {
            cliHelper.printError("No se pudo deshacer: " + e.getMessage());
        }
    }

    // Opción 7: rehacer (redo)
    private static void opcionRedo() {
        try {
            if(!controller.getActionStack().getRedoStack().isEmpty()){
                IAction accion = controller.getActionStack().getRedoStack().peek();
                controller.redo();
                cliHelper.printSuccess("✓(Rehacer)✓ Acción rehecha: " + accion.toString());
                return;
            }
            cliHelper.printAlert("!(No hay acciones por rehacer)!");
        } catch (Exception e) {
            cliHelper.printError("No se pudo rehacer: " + e.getMessage());
        }
    }

    // Opción 8: generación de reportes
    private static void opcionGenerarReporte() {
        System.out.println("-- Generar reportes --");
        System.out.println("1) Pendientes por tipo");
        System.out.println("2) Completados");
        System.out.println("3) Top K por notas");
        System.out.println("4) Volver");
        int r = askInt("Elija: ", 1, 4);
        switch (r) {
            case 1:
                boolean saveCsv = askYesNo("¿Exportar CSV? (s/n): ");
                String path = null;
                if (saveCsv) path = askString("Nombre del archivo (ej: pendiente_por_tipo): ");
                controller.generateReportPendingByType(saveCsv, path);
                cliHelper.printSuccess("Reporte generado.");
                break;
            case 2:
                try {
                    controller.getReportManager().showCompleted(controller.getAttentionQueue().getAttendedHistory(), true, "completed");
                    cliHelper.printSuccess("Reporte completados generado.");
                } catch (Exception e) {
                    cliHelper.printError("Error generando reporte completados: " + e.getMessage());
                }
                break;
            case 3:
                int k = askInt("Top K (k): ", 1, 100);
                try {
                    controller.getReportManager().showTopKByNotes(controller.buildPendingSnapshotFromQueues(), k, true, "topk");
                    cliHelper.printSuccess("Top K generado.");
                } catch (Exception e) {
                    cliHelper.printError("Error al generar TopK: " + e.getMessage());
                }
                break;
            case 4:
                break;
        }
    }

    // ---------- Utilitarios ----------
    // Construye una línea legible para un ticket (uso en listados)
    private static String formatTicketLine(Ticket t) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(t.getId())
                .append(" | ").append(t.getStudent())
                .append(" | ").append(t.getProcedureType())
                .append(" | Estado: ").append(t.getState())
                .append(" | Notas:");
        for(int i = 0; i < t.getNoteHistory().size(); i++) {
                sb.append("\n\t- ");
            Note n = t.getNoteHistory().findByIndex(i);
            sb.append("[").append(n.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" - ").append(n.getObservation()).append("]");
        }
        return sb.toString();
    }

    // Lectura robusta de strings no vacíos
    private static String askString(String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        while (s.isEmpty()) {
            cliHelper.printError("!(This field cannot be empty)!");
            System.out.print(prompt);
            s = scanner.nextLine().trim();
        }
        return s;
    }

    // Lectura y validación de enteros en rango
    private static int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    cliHelper.printError("!(Please select a valid option)!");
                } else {
                    return v;
                }
            } catch (NumberFormatException e) {
                cliHelper.printError("!(Please enter a number)!");
            }
        }
    }

    // Pregunta sí/no simple (acepta s/n, yes/no)
    private static boolean askYesNo(String prompt) {
        while (true) {
            cliHelper.printInfo(prompt);
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("y") || r.equals("yes")) return true;
            if (r.equals("n") || r.equals("no")) return false;
            cliHelper.printAlert("Responda 's' o 'n'.");
        }
    }

    // Selección interactiva de tipo de trámite
    private static ProcedureType askProcedureType() {
        ProcedureType[] types = ProcedureType.values();
        System.out.println("Seleccione tipo de trámite:");
        for (int i = 0; i < types.length; i++) {
            System.out.println(" " + (i+1) + ") " + types[i].toString());
        }
        int sel = askInt("Elija: ", 1, types.length);
        return types[sel-1];
    }

    // Selección interactiva de estado de ticket
    private static TicketState askTicketState() {
        TicketState[] states = TicketState.values();
        System.out.println("Seleccione nuevo estado:");
        for (int i = 0; i < states.length; i++) {
            System.out.println(" " + (i+1) + ") " + states[i]);
        }
        int sel = askInt("Elija: ", 1, states.length);
        return states[sel-1];
    }
}