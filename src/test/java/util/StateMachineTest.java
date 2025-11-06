package util;

import domine.TicketState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private StateMachine sm;

    @BeforeEach
    void setUp() {
        sm = new StateMachine();
    }

    @Test
    @DisplayName("Transiciones válidas por defecto")
    void validDefaultTransitions() {
        assertTrue(sm.isValidTransition(TicketState.EN_COLA, TicketState.EN_ATENCION));
        assertTrue(sm.isValidTransition(TicketState.EN_COLA, TicketState.URGENTE));
        assertTrue(sm.isValidTransition(TicketState.URGENTE, TicketState.EN_ATENCION));
        assertTrue(sm.isValidTransition(TicketState.EN_ATENCION, TicketState.COMPLETADO));
    }

    @Test
    @DisplayName("Transiciones inválidas por defecto")
    void invalidDefaultTransitions() {
        assertFalse(sm.isValidTransition(TicketState.EN_COLA, TicketState.COMPLETADO));
        assertFalse(sm.isValidTransition(TicketState.COMPLETADO, TicketState.EN_COLA));
    }

    @Test
    @DisplayName("allowedNextStates retorna los estados correctos")
    void allowedNextStates() {
        List<TicketState> fromEnCola = sm.allowedNextStates(TicketState.EN_COLA);
        // EN_COLA -> EN_ATENCION, URGENTE, PENDIENTE_DOCS
        assertEquals(3, fromEnCola.size());
        assertTrue(fromEnCola.contains(TicketState.EN_ATENCION));
        assertTrue(fromEnCola.contains(TicketState.URGENTE));
        assertTrue(fromEnCola.contains(TicketState.PENDIENTE_DOCS));
    }
}