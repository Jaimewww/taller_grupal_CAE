package controller;

import domine.Ticket;
import domine.TicketState;
import domine.ProcedureType;
import domine.Note;
import controller.command.AddTicketCommand;
import controller.command.AddNoteCommand;
import controller.command.CloseCaseCommand;
import persistence.PersistenceManager;
import reports.ReportManager;
import util.StateMachine;
import util.SystemClock;
import estructures.AttentionQueue;
import estructures.Queue;
import estructures.SimpleList;
import estructures.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * CaeController - versión final y limpia.
 * - Asume AttentionQueue.nextTicket() hace peek (no remueve).
 * - Usa AddTicketCommand, AddNoteCommand, CloseCaseCommand.
 * - Usa CLIHelper para todos los mensajes.
 * - Sin adaptadores, sin reflexión.
 */
public class CaeController {

    private final AttentionQueue attentionQueue;
    private final ActionStack actionStack;
    private final PersistenceManager persistenceManager;
    private final ReportManager reportManager;
    private final StateMachine stateMachine;
    private final SystemClock clock;
    private final CLIHelper cli;

    public CaeController(AttentionQueue attentionQueue,
                         ActionStack actionStack,
                         PersistenceManager persistenceManager,
                         ReportManager reportManager,
                         StateMachine stateMachine,
                         SystemClock clock,
                         CLIHelper cli) {
        this.attentionQueue = attentionQueue;
        this.actionStack = actionStack;
        this.persistenceManager = persistenceManager;
        this.reportManager = reportManager;
        this.stateMachine = stateMachine;
        this.clock = clock;
        this.cli = cli;
    }

    // ------------------ Ciclo de vida ------------------

    public void start() {
        try {
            Queue<Ticket> pendingFromDisk = new Queue<>();
            Queue<Ticket> historyFromDisk = new Queue<>();
            persistenceManager.loadData(pendingFromDisk, historyFromDisk);

            while (!pendingFromDisk.isEmpty()) {
                Ticket t = pendingFromDisk.dequeue();
                try {
                    attentionQueue.addTicket(t);
                } catch (Exception inner) {
                    cli.printAlert("No se pudo agregar ticket ID=" + t.getId() + " durante carga: " + inner.getMessage());
                }
            }

            while (!historyFromDisk.isEmpty()) {
                Ticket t = historyFromDisk.dequeue();
                try {
                    attentionQueue.moveToHistory(t);
                } catch (Exception inner) {
                    cli.printAlert("No se pudo mover a historial ticket ID=" + t.getId() + ": " + inner.getMessage());
                }
            }

            cli.printSuccess("Sistema cargado correctamente.");
        } catch (Exception ex) {
            cli.printError("Error al cargar datos: " + ex.getMessage());
        }
    }

    public void shutdown() {
        try {
            SimpleList<Ticket> pendingSnapshot = buildPendingSnapshotFromQueues();
            persistenceManager.saveTickets(pendingSnapshot);

            SimpleList<Ticket> history = attentionQueue.getAttendedHistory();
            persistenceManager.saveHistory(history);

            saveAllTicketNotes(pendingSnapshot);
            saveAllTicketNotes(history);

            cli.printSuccess("Estado guardado correctamente.");
        } catch (Exception ex) {
            cli.printError("Error al persistir datos en shutdown: " + ex.getMessage());
        }
    }

    // ------------------ Operaciones principales ------------------

    public Ticket createTicket(String student, ProcedureType type, boolean urgentFlag) {
        if (student == null || student.trim().isEmpty()) {
            cli.printError("!(This field cannot be empty)!");
            throw new IllegalArgumentException("El nombre del estudiante no puede ser vacío");
        }

        Ticket t = new Ticket(student, type);
        t.setState(urgentFlag ? TicketState.URGENTE : TicketState.EN_COLA);
        t.setId(attentionQueue.getTotalWaiting() + attentionQueue.getAttendedHistory().size() + 1);

        try {
            Queue<Ticket> targetQueue = urgentFlag ? attentionQueue.getUrgentQueue() : attentionQueue.getNormalQueue();
            if (targetQueue == null) {
                attentionQueue.addTicket(t);
                cli.printAlert("Cola interna no disponible; ticket añadido via attentionQueue.addTicket().");
            } else {
                AddTicketCommand cmd = new AddTicketCommand(targetQueue, t);
                cmd.execute();
                actionStack.registerAction(cmd);
            }

            try {
                persistenceManager.saveTickets(buildPendingSnapshotFromQueues());
            } catch (Exception pex) {
                cli.printAlert("Advertencia: no se pudo persistir inmediatamente: " + pex.getMessage());
            }

            cli.printSuccess("✓(Ticket creado)✓ ID=" + t.getId());
            return t;
        } catch (Exception ex) {
            cli.printError("Error creando ticket: " + ex.getMessage());
            throw new RuntimeException("createTicket failed", ex);
        }
    }

    /**
     * request the next ticket to attend (peek only). Caller (UI) will present and then call finalizeTicket()
     * when they want to close the case.
     */
    public Ticket attendNext() {
        try {
            Ticket next = attentionQueue.nextTicket(); // peek
            if (next == null) {
                cli.printInfo("No hay tickets pendientes.");
                throw new NoSuchElementException("No hay tickets pendientes");
            }

            TicketState prev = next.getState();
            if (!stateMachine.isValidTransition(prev, TicketState.EN_ATENCION)) {
                cli.printAlert("Transición no permitida a EN_ATENCION desde " + prev);
                throw new IllegalStateException("Transición inválida");
            }

            next.setState(TicketState.EN_ATENCION);
            cli.printInfo("Atendiendo ticket ID=" + next.getId() + " — alumno: " + next.getStudent());
            return next;
        } catch (NoSuchElementException ex) {
            throw ex;
        } catch (Exception ex) {
            cli.printError("Error al obtener siguiente ticket: " + ex.getMessage());
            throw new RuntimeException("attendNext failed", ex);
        }
    }

    public void finalizeTicket(Ticket ticket) {
        if (ticket == null) {
            cli.printError("Ticket nulo al intentar finalizar.");
            throw new IllegalArgumentException("ticket no puede ser null");
        }

        try {
            if (!stateMachine.isValidTransition(ticket.getState(), TicketState.COMPLETADO)) {
                cli.printAlert("Transición inválida a COMPLETADO desde " + ticket.getState());
                throw new IllegalStateException("Transición inválida");
            }

            Queue<Ticket> sourceQueue = findSourceQueueForTicket(ticket);
            if (sourceQueue == null) {
                cli.printAlert("No se pudo identificar la cola origen del ticket; se intentará finalizar de todas formas.");
                sourceQueue = attentionQueue.getNormalQueue(); // fallback razonable
            }

            SimpleList<Ticket> attendedHistory = attentionQueue.getAttendedHistory();

            CloseCaseCommand closeCmd = new CloseCaseCommand(ticket, sourceQueue, attendedHistory);
            closeCmd.execute();
            actionStack.registerAction(closeCmd);

            try {
                persistenceManager.saveHistory(attentionQueue.getAttendedHistory());
                persistenceManager.saveTickets(buildPendingSnapshotFromQueues());
            } catch (Exception pex) {
                cli.printAlert("Advertencia: no se pudo persistir inmediatamente tras finalizar: " + pex.getMessage());
            }

            cli.printSuccess("✓(Ticket finalizado)✓ ID=" + ticket.getId());
        } catch (Exception ex) {
            cli.printError("Error finalizando ticket ID=" + ticket.getId() + ": " + ex.getMessage());
            throw new RuntimeException("finalizeTicket failed", ex);
        }
    }

    public Note addNoteToTicket(Ticket ticket, String observation) {
        if (ticket == null) {
            cli.printError("Ticket nulo al agregar nota.");
            throw new IllegalArgumentException("ticket no puede ser null");
        }
        if (observation == null || observation.trim().isEmpty()) {
            cli.printError("!(This field cannot be empty)!");
            throw new IllegalArgumentException("observación vacía");
        }

        try {
            Note note = new Note(observation);
            AddNoteCommand cmd = new AddNoteCommand(ticket, note);
            cmd.execute();
            actionStack.registerAction(cmd);

            try {
                persistenceManager.saveNotesForTicket(ticket);
            } catch (Exception pex) {
                cli.printAlert("Advertencia: no se pudo persistir las notas inmediatamente: " + pex.getMessage());
            }

            cli.printSuccess("✓(Nota agregada)✓ Ticket ID=" + ticket.getId());
            return note;
        } catch (Exception ex) {
            cli.printError("Error agregando nota a ticket ID=" + ticket.getId() + ": " + ex.getMessage());
            throw new RuntimeException("addNoteToTicket failed", ex);
        }
    }

    public void changeTicketState(int ticketId, TicketState newState) {
        try {
            Ticket t = findTicketById(ticketId);
            if (t == null) {
                cli.printError("Ticket no encontrado: " + ticketId);
                throw new NoSuchElementException("Ticket no encontrado");
            }
            TicketState from = t.getState();
            if (!stateMachine.isValidTransition(from, newState)) {
                cli.printAlert("Transición inválida: " + from + " -> " + newState);
                throw new IllegalStateException("Transición inválida");
            }
            if(newState == TicketState.EN_ATENCION && !attentionQueue.getUrgentQueue().isEmpty() && !attentionQueue.getNormalQueue().isEmpty()) {

                    if(attentionQueue.getNormalQueue().peek().getState() == TicketState.EN_ATENCION || attentionQueue.getUrgentQueue().peek().getState() == TicketState.EN_ATENCION) {
                        cli.printAlert("Ya hay un ticket en atención. No se puede cambiar el estado.");
                        throw new IllegalStateException("Ya hay un ticket en atención");
                    }

            }

            if(newState == TicketState.URGENTE) {
                attentionQueue.getNormalQueue().remove(t);
                attentionQueue.getUrgentQueue().enqueue(t);
            }

            if(t.getState() == TicketState.URGENTE && newState == TicketState.EN_COLA) {
                attentionQueue.getUrgentQueue().remove(t);
                attentionQueue.getNormalQueue().enqueue(t);
            }

            t.setState(newState);

            try {
                persistenceManager.saveTickets(buildPendingSnapshotFromQueues());
                persistenceManager.saveHistory(attentionQueue.getAttendedHistory());
            } catch (Exception pex) {
                cli.printAlert("Advertencia: error al persistir tras cambio de estado: " + pex.getMessage());
            }

            cli.printSuccess("✓(Estado cambiado)✓ ID=" + ticketId + ": " + from + " -> " + newState);
        } catch (Exception ex) {
            throw ex;
        }
    }

    // ------------------ Undo/Redo ------------------

    public void undo() {
        try {
            actionStack.undo();
            try {
                persistenceManager.saveTickets(buildPendingSnapshotFromQueues());
                persistenceManager.saveHistory(attentionQueue.getAttendedHistory());
            } catch (Exception pex) {
                cli.printAlert("Advertencia: error al persistir tras undo: " + pex.getMessage());
            }
            cli.printSuccess("✓(Undo realizado)✓");
        } catch (Exception ex) {
            cli.printError("No se pudo realizar undo: " + ex.getMessage());
        }
    }

    public void redo() {
        try {
            actionStack.redo();
            try {
                persistenceManager.saveTickets(buildPendingSnapshotFromQueues());
                persistenceManager.saveHistory(attentionQueue.getAttendedHistory());
            } catch (Exception pex) {
                cli.printAlert("Advertencia: error al persistir tras redo: " + pex.getMessage());
            }
            cli.printSuccess("✓(Redo realizado)✓");
        } catch (Exception ex) {
            cli.printError("No se pudo realizar redo: " + ex.getMessage());
        }
    }

    // ------------------ Reportes / Consultas ------------------

    public List<Ticket> listPending() {
        List<Ticket> out = new ArrayList<>();
        try {
            Queue<Ticket> urgentQ = attentionQueue.getUrgentQueue();
            Queue<Ticket> normalQ = attentionQueue.getNormalQueue();
            if (urgentQ != null) out.addAll(snapshotQueuePreserve(urgentQ));
            if (normalQ != null) out.addAll(snapshotQueuePreserve(normalQ));
        } catch (Exception ex) {
            cli.printError("Error listando pendientes: " + ex.getMessage());
        }
        return out;
    }

    public void generateReportPendingByType(boolean exportCsv, String csvPath) {
        try {
            SimpleList<Ticket> snapshot = buildPendingSnapshotFromQueues();
            reportManager.showPendingByType(snapshot, exportCsv, csvPath);
            cli.printInfo("Reporte generado en pantalla.");
            if (exportCsv) cli.printSuccess("CSV guardado en: " + csvPath);
        } catch (Exception ex) {
            cli.printError("Error generando reporte: " + ex.getMessage());
        }
    }

    // ------------------ Helpers privados ------------------

    private Queue<Ticket> findSourceQueueForTicket(Ticket ticket) {
        try {
            Queue<Ticket> urgent = attentionQueue.getUrgentQueue();
            if (urgent != null) {
                try {
                    Ticket found = urgent.find(ticket);
                    if (found != null) return urgent;
                } catch (NoSuchElementException ignored) {}
            }
            Queue<Ticket> normal = attentionQueue.getNormalQueue();
            if (normal != null) {
                try {
                    Ticket found = normal.find(ticket);
                    if (found != null) return normal;
                } catch (NoSuchElementException ignored) {}
            }
        } catch (Exception ex) {
            cli.printAlert("Advertencia buscando cola origen: " + ex.getMessage());
        }
        return null;
    }

    public SimpleList<Ticket> buildPendingSnapshotFromQueues() {
        SimpleList<Ticket> sl = new SimpleList<>();
        try {
            Queue<Ticket> urgentQ = attentionQueue.getUrgentQueue();
            Queue<Ticket> normalQ = attentionQueue.getNormalQueue();
            if (urgentQ != null) {
                List<Ticket> list = snapshotQueuePreserve(urgentQ);
                for (Ticket t : list) sl.pushBack(t);
            }
            if (normalQ != null) {
                List<Ticket> list = snapshotQueuePreserve(normalQ);
                for (Ticket t : list) sl.pushBack(t);
            }
        } catch (Exception ex) {
            cli.printAlert("Advertencia al construir snapshot: " + ex.getMessage());
        }
        return sl;
    }

    private List<Ticket> snapshotQueuePreserve(Queue<Ticket> q) {
        List<Ticket> res = new ArrayList<>();
        if (q == null) return res;
        Queue<Ticket> tmp = new Queue<>();
        while (!q.isEmpty()) {
            Ticket t = q.dequeue();
            res.add(t);
            tmp.enqueue(t);
        }
        while (!tmp.isEmpty()) q.enqueue(tmp.dequeue());
        return res;
    }

    private void saveAllTicketNotes(SimpleList<Ticket> tickets) {
        if (tickets == null) return;
        Node<Ticket> node = tickets.head;
        while (node != null) {
            try {
                persistenceManager.saveNotesForTicket(node.value);
            } catch (Exception ex) {
                cli.printAlert("No se pudo guardar notas ticket ID=" + node.value.getId() + ": " + ex.getMessage());
            }
            node = node.next;
        }
    }

    public Ticket findTicketById(int ticketId) {
        try {
            Queue<Ticket> urgentQ = attentionQueue.getUrgentQueue();
            if (urgentQ != null) {
                for (Ticket t : snapshotQueuePreserve(urgentQ)) if (t.getId() == ticketId) return t;
            }
            Queue<Ticket> normalQ = attentionQueue.getNormalQueue();
            if (normalQ != null) {
                for (Ticket t : snapshotQueuePreserve(normalQ)) if (t.getId() == ticketId) return t;
            }
            SimpleList<Ticket> history = attentionQueue.getAttendedHistory();
            if (history != null) {
                Node<Ticket> n = history.head;
                while (n != null) {
                    if (n.value.getId() == ticketId) return n.value;
                    n = n.next;
                }
            }
        } catch (Exception ex) {
            cli.printAlert("Advertencia al buscar ticket por ID: " + ex.getMessage());
        }
        return null;
    }

    // ------------------ Accesores ------------------

    public AttentionQueue getAttentionQueue() { return attentionQueue; }
    public ActionStack getActionStack() { return actionStack; }
    public PersistenceManager getPersistenceManager() { return persistenceManager; }
    public ReportManager getReportManager() { return reportManager; }
    public StateMachine getStateMachine() { return stateMachine; }
    public SystemClock getClock() { return clock; }
    public CLIHelper getCliHelper() { return cli; }
}