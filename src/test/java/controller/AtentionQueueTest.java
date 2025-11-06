package controller;

import domine.ProcedureType;
import domine.Ticket;
import domine.TicketState;
import estructures.AttentionQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AtentionQueueTest {

    private AttentionQueue attentionQueue;
    private Ticket urgentTicket;
    private Ticket normalTicket;

    @BeforeEach
    void setUp() {
        attentionQueue = new AttentionQueue();

        urgentTicket = new Ticket("Estudiante Urgente", ProcedureType.OTRO);
        urgentTicket.setState(TicketState.URGENTE); // Estado URGENTE

        normalTicket = new Ticket("Estudiante Normal", ProcedureType.MATRICULA);
        // El estado por defecto es EN_COLA
    }

    @Test
    @DisplayName("Agrega tickets a las colas correctas")
    void addTicketRoutesToCorrectQueue() {
        attentionQueue.addTicket(normalTicket);
        attentionQueue.addTicket(urgentTicket);

        assertEquals(1, attentionQueue.getNormalQueue().size());
        assertEquals(1, attentionQueue.getUrgentQueue().size());
        assertEquals(2, attentionQueue.getTotalWaiting());
    }

    @Test
    @DisplayName("nextTicket() da prioridad a URGENTE")
    void nextTicketPrioritizesUrgent() {
        attentionQueue.addTicket(normalTicket);
        attentionQueue.addTicket(urgentTicket);

        // Debe sacar el URGENTE primero
        Ticket next = attentionQueue.nextTicket();

        assertEquals(urgentTicket, next);
        assertEquals("Estudiante Urgente", next.getStudent());
    }

    @Test
    @DisplayName("nextTicket() saca NORMAL si no hay urgentes")
    void nextTicketTakesNormalWhenNoUrgent() {
        attentionQueue.addTicket(normalTicket);

        Ticket next = attentionQueue.nextTicket();
        assertEquals(normalTicket, next);
        assertEquals("Estudiante Normal", next.getStudent());
    }
}