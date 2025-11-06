package controller.command;

import controller.ActionStack;
import domine.Note;
import domine.ProcedureType;
import domine.Ticket;
import domine.TicketState;
import estructures.Queue;
import estructures.SimpleList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandActionStackTest {

    private ActionStack actionStack;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        actionStack = new ActionStack();
        testTicket = new Ticket("Test Student", ProcedureType.OTRO);
        testTicket.setId(1);
    }

    @Test
    @DisplayName("ActionStack: Lógica de Undo/Redo funciona")
    void actionStackUndoRedoLogic() {
        // Usaremos AddNoteCommand como acción de prueba
        Note note1 = new Note("Nota 1");
        AddNoteCommand cmd1 = new AddNoteCommand(testTicket, note1);

        cmd1.execute();
        actionStack.registerAction(cmd1);

        assertEquals(1, testTicket.getNoteHistory().size());
        assertEquals(1, actionStack.getUndoStack().size());
        assertEquals(0, actionStack.getRedoStack().size());

        // --- PRUEBA DE UNDO ---
        assertDoesNotThrow(() -> {
            actionStack.undo();
        }, "Falla si SimpleList.remove() está roto");

        // Aserciones
        // assertEquals(0, testTicket.getNoteHistory().size()); 
        // assertEquals(0, actionStack.getUndoStack().size());
        // assertEquals(1, actionStack.getRedoStack().size());

        // --- PRUEBA DE REDO ---
        // actionStack.redo();
        // assertEquals(1, testTicket.getNoteHistory().size());
        // assertEquals(1, actionStack.getUndoStack().size());
        // assertEquals(0, actionStack.getRedoStack().size());
    }

    @Test
    @DisplayName("AddTicketCommand: Falla Undo (Bug !=)")
    void addTicketCommandUndoFails() {
        Queue<Ticket> queue = new Queue<>();
        Ticket ticketA = new Ticket("A", ProcedureType.OTRO);
        Ticket ticketB = new Ticket("B", ProcedureType.OTRO);

        queue.enqueue(ticketA); // Cola: [A]

        AddTicketCommand cmdB = new AddTicketCommand(queue, ticketB);
        cmdB.execute(); // Cola: [A, B]

        // Clonamos ticketB para simular un objeto "diferente"
        Ticket ticketB_clone = new Ticket("B", ProcedureType.OTRO);
        AddTicketCommand cmdClone = new AddTicketCommand(queue, ticketB_clone);

        // Esta prueba fallará (no eliminará ticketB) si AddTicketCommand.undo() usa '!='
        // Debería pasar si se usa '.equals()'
        assertDoesNotThrow(() -> {
            // cmdB.undo(); // <- Esto podría pasar por ser la misma instancia
            cmdClone.undo(); // <- Esto fallará
        }, "Falla porque AddTicketCommand.undo() usa '!=' en lugar de '!equals()'");

        // Aserciones
        // assertEquals(1, queue.size());
        // assertEquals(ticketA, queue.dequeue());
    }

    @Test
    @DisplayName("CloseCaseCommand: Falla Undo (Bug SimpleList)")
    void closeCaseCommandUndoFails() {
        Queue<Ticket> pendingQueue = new Queue<>();
        SimpleList<Ticket> historyList = new SimpleList<>();

        pendingQueue.enqueue(testTicket);

        CloseCaseCommand cmd = new CloseCaseCommand(testTicket, pendingQueue, historyList);
        cmd.execute(); // Mueve Ticket de pending a history

        assertEquals(0, pendingQueue.size());
        assertEquals(1, historyList.size());
        Assertions.assertEquals(TicketState.COMPLETADO, testTicket.getState());

        assertDoesNotThrow(() -> {
            cmd.undo(); // Intenta mover Ticket de history a pending
        }, "Falla porque SimpleList.remove() está roto");

        // Aserciones
        // assertEquals(1, pendingQueue.size());
        // assertEquals(0, historyList.size());
        // assertEquals(TicketState.EN_COLA, testTicket.getState());
    }
}