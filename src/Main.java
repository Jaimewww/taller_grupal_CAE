// Main.java
import java.util.*;
import java.time.format.DateTimeFormatter;

// importa paquetes del proyecto (ajusta los nombres de paquete si son distintos)
import domine.*;
import estructures.*;
import persistence.*;
import reports.*;
import util.*;
import controller.*;

/**
 * Main ajustado a la firma real del constructor de CaeController.
 *
 * Si alguna de las dependencias no tiene constructor por defecto,
 * ajusta la creación correspondiente comentada más abajo.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static CaeController controller;

    public static void main(String[] args) {
        try {
            // ----------------------------
            // Instanciar dependencias
            // ----------------------------

            // Atención / colas
            AttentionQueue attentionQueue = new AttentionQueue(); // (usa constructor por defecto)

            // Pila de acciones (undo/redo)
            ActionStack actionStack = new ActionStack(); // constructor por defecto según diseño

            // Persistencia
            PersistenceManager persistenceManager;
            try {
                persistenceManager = new PersistenceManager(); // si requiere path, ajusta: new PersistenceManager("data/")
            } catch (Throwable t) {
                // fallback minimal si no existe constructor por defecto
                persistenceManager = null;
                System.err.println("Aviso: PersistenceManager no pudo instanciarse por defecto. Ajusta el Main.java. >> " + t.getMessage());
            }

            // Reportes
            ReportManager reportManager;
            try {
                reportManager = new ReportManager();
            } catch (Throwable t) {
                reportManager = null;
                System.err.println("Aviso: ReportManager no pudo instanciarse por defecto. Ajusta el Main.java. >> " + t.getMessage());
            }

            // State machine
            StateMachine stateMachine = new StateMachine();

            // Reloj del sistema (abstracción)
            SystemClock clock = new SystemClock();

            // Helper de CLI (para que controller lo use internamente)
            CLIHelper cliHelper;
            try {
                cliHelper = new CLIHelper();
            } catch (Throwable t) {
                // Si CLIHelper requiere parámetros, deja una instancia mínima (null es desaconsejado).
                cliHelper = null;
                System.err.println("Aviso: CLIHelper no pudo instanciarse por defecto. Ajusta el Main.java. >> " + t.getMessage());
            }

            // ----------------------------
            // Crear controlador con la firma requerida
            // ----------------------------
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
                System.err.println("Aviso: error al invocar start() del controlador: " + ex.getMessage());
            }

            // ----------------------------
            // Bucle principal (CLI simple)
            // ----------------------------
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
                        System.out.println("Opción inválida.");
                }
            }

            // Guardar / cerrar
            try {
                controller.shutdown();
            } catch (NoSuchMethodError | AbstractMethodError e) {
                // ignore
            } catch (Exception ex) {
                System.err.println("Error al guardar/cerrar: " + ex.getMessage());
            }

            System.out.println("✓(Operación finalizada)✓ - Saliendo...");

        } catch (Throwable t) {
            System.err.println("Error crítico en la inicialización: " + t.getMessage());
            t.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // ---------------------------------------------------------
    // Menú y acciones (usa controller con la API documentada)
    // ---------------------------------------------------------
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

    private static void opcionCrearTicket() {
        System.out.println("-- Crear nuevo ticket --");
        String student = askString("Nombre del estudiante: ");
        ProcedureType type = askProcedureType();
        boolean urgent = askYesNo("¿Prioridad urgente? (s/n): ");
        try {
            Ticket t = controller.createTicket(student, type, urgent);
            System.out.println("✓(Ticket creado)✓ ID: " + t.getId() + " - " + t.getStudent() + " (" + t.getProcedureType() + ")");
        } catch (Exception e) {
            System.err.println("Error al crear ticket: " + e.getMessage());
        }
    }

    private static void opcionAtenderSiguiente() {
        System.out.println("-- Atender siguiente --");
        try {
            Ticket t = controller.attendNext();
            if (t == null) {
                System.out.println("?(info)? No hay tickets por atender.");
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
                            System.out.println("✓(Nota añadida)✓ " + n.toString());
                        } catch (Exception ex) {
                            System.err.println("Error al añadir nota: " + ex.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            controller.changeTicketState(t.getId(), TicketState.PENDIENTE_DOCS);
                            System.out.println("✓(Estado cambiado)✓ -> PENDIENTE_DOCS");
                        } catch (Exception ex) {
                            System.err.println("No se pudo cambiar estado: " + ex.getMessage());
                        }
                        atendiendo = false;
                        break;
                    case 3:
                        try {
                            controller.finalizeTicket(t);
                            System.out.println("✓(Ticket finalizado)✓ -> COMPLETADO");
                        } catch (Exception ex) {
                            System.err.println("Error al finalizar: " + ex.getMessage());
                        }
                        atendiendo = false;
                        break;
                    case 4:
                        atendiendo = false;
                        break;
                }
            }

        } catch (NoSuchElementException ne) {
            System.out.println("?(info)? No hay tickets por atender.");
        } catch (Exception e) {
            System.err.println("Error en atender siguiente: " + e.getMessage());
        }
    }

    private static void opcionListarPendientes() {
        System.out.println("-- Tickets pendientes (snapshot) --");
        try {
            List<Ticket> pendientes = controller.listPending();
            if (pendientes == null || pendientes.isEmpty()) {
                System.out.println("?(info)? No hay tickets pendientes.");
                return;
            }
            for (Ticket t : pendientes) {
                System.out.println(formatTicketLine(t));
            }
        } catch (Exception e) {
            System.err.println("Error al listar pendientes: " + e.getMessage());
        }
    }

    private static void opcionConsultarHistorial() {
        System.out.println("-- Historial (completados) --");
        try {
            SimpleList<Ticket> history = controller.getAttentionQueue().getAttendedHistory();
            if (history == null || history.isEmpty()) {
                System.out.println("?(info)? No hay historial registrado.");
                return;
            }
            Node<Ticket> cursor = history.head;
            while (cursor != null) {
                System.out.println(formatTicketLine(cursor.value));
                cursor = cursor.next;
            }
        } catch (Exception e) {
            System.err.println("Error al consultar historial: " + e.getMessage());
        }
    }

    private static void opcionCambiarEstado() {
        System.out.println("-- Cambiar estado de ticket --");
        int id = askInt("ID del ticket: ", 1, Integer.MAX_VALUE);
        try {
            Ticket t = controller.findTicketById(id);
            if (t == null) {
                System.out.println("!(error)! Ticket no encontrado.");
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
                System.out.println("✓(Estado cambiado)✓ -> " + target);
            } catch (Exception ex) {
                System.out.println("No se pudo consultar StateMachine; seleccione estado manualmente:");
                TicketState target = askTicketState();
                controller.changeTicketState(id, target);
                System.out.println("✓(Estado cambiado)✓ -> " + target);
            }
        } catch (Exception e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
        }
    }

    private static void opcionUndo() {
        try {
            controller.undo();
            System.out.println("✓(Deshacer)✓ Acción deshecha.");
        } catch (Exception e) {
            System.err.println("No se pudo deshacer: " + e.getMessage());
        }
    }

    private static void opcionRedo() {
        try {
            controller.redo();
            System.out.println("✓(Rehacer)✓ Acción rehecha.");
        } catch (Exception e) {
            System.err.println("No se pudo rehacer: " + e.getMessage());
        }
    }

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
                if (saveCsv) path = askString("Ruta CSV (ej: data/pending_by_type.csv): ");
                // Usa reportManager via controller (firma sugerida: generateReportPendingByType)
                controller.generateReportPendingByType(saveCsv, path);
                System.out.println("Reporte generado.");
                break;
            case 2:
                try {
                    controller.getReportManager().showCompleted(controller.getAttentionQueue().getAttendedHistory(), true, "data/completed.csv");
                    System.out.println("Reporte completados generado.");
                } catch (Exception e) {
                    System.err.println("Error generando reporte completados: " + e.getMessage());
                }
                break;
            case 3:
                int k = askInt("Top K (k): ", 1, 100);
                try {
                    controller.getReportManager().showTopKByNotes(controller.buildPendingSnapshotFromQueues(), k, true, "data/topk.csv");
                    System.out.println("Top K generado.");
                } catch (Exception e) {
                    System.err.println("Error al generar TopK: " + e.getMessage());
                }
                break;
            case 4:
                break;
        }
    }

    // ---------- Utilitarios ----------
    private static String formatTicketLine(Ticket t) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(t.getId())
                .append(" | ").append(t.getStudent())
                .append(" | ").append(t.getProcedureType())
                .append(" | Estado: ").append(t.getState());
        return sb.toString();
    }

    private static String askString(String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        while (s.isEmpty()) {
            System.out.println("!(This field cannot be empty)!");
            System.out.print(prompt);
            s = scanner.nextLine().trim();
        }
        return s;
    }

    private static int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.println("!(Please select a valid option)!");
                } else {
                    return v;
                }
            } catch (NumberFormatException e) {
                System.out.println("!(Please enter a number)!");
            }
        }
    }

    private static boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("y") || r.equals("yes")) return true;
            if (r.equals("n") || r.equals("no")) return false;
            System.out.println("Responda 's' o 'n'.");
        }
    }

    private static ProcedureType askProcedureType() {
        ProcedureType[] types = ProcedureType.values();
        System.out.println("Seleccione tipo de trámite:");
        for (int i = 0; i < types.length; i++) {
            System.out.println(" " + (i+1) + ") " + types[i].toString());
        }
        int sel = askInt("Elija: ", 1, types.length);
        return types[sel-1];
    }

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