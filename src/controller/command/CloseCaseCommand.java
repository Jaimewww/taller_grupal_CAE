package controller.command;

import controller.IAction;
import domine.Ticket;
import domine.TicketState;
import estructures.Queue;
import estructures.SimpleList;

public class CloseCaseCommand implements IAction {
    private Ticket ticket;
    private TicketState previousState;
    private Queue<Ticket> sourceQueue;
    private SimpleList<Ticket> attendedHistory;

    /**
     * El constructor ahora recibe la cola de la que proviene el ticket.
     * El "llamador" (quien crea el comando) es responsable de pasar
     * la cola de 'urgentes' o la de 'en_cola' en el parámetro 'sourceQueue'.
     */
    public CloseCaseCommand(Ticket ticket, Queue<Ticket> sourceQueue, SimpleList<Ticket> attendedHistory ) {
        this.ticket = ticket;
        this.sourceQueue = sourceQueue;
        this.attendedHistory = attendedHistory;
        
        this.previousState = ticket.getState();
    }

    @Override
    public void execute() {
        sourceQueue.remove(ticket);

        attendedHistory.pushBack(ticket);
        ticket.setState(TicketState.COMPLETADO);
    }

    @Override
    public void undo() {
        ticket.setState(this.previousState);

        attendedHistory.remove(ticket);

        sourceQueue.enqueue(ticket);
    }
}