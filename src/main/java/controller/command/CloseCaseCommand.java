package controller.command;

import controller.IAction;
import domine.Ticket;
import domine.TicketState;
import estructures.Node;
import estructures.Queue;
import estructures.SimpleList;

/**
 * Esta clase representa un comando para cerrar un ticket.
 * Implementa la interfaz IAction, que define los métodos execute y undo.
 * @author Jaime Landázuri
 * */

public class CloseCaseCommand implements IAction {
    private Ticket ticket;
    private Queue<Ticket> sourceQueue;
    private SimpleList<Ticket> attendedHistory;

    /**
     * El constructor recibe la cola de la que proviene el ticket.
     * El "llamador" (quien crea el comando) es responsable de pasar
     * la cola de 'urgentes' o la de 'en_cola' en el parámetro 'sourceQueue'.
     */
    public CloseCaseCommand(Ticket ticket, Queue<Ticket> sourceQueue, SimpleList<Ticket> attendedHistory ) {
        this.ticket = ticket;
        this.sourceQueue = sourceQueue;
        this.attendedHistory = attendedHistory;
    }

    @Override
    public void execute() {
        sourceQueue.remove(ticket);

        attendedHistory.pushBack(ticket);
        ticket.setState(TicketState.COMPLETADO);
    }

    @Override
    public void undo() {
        ticket.setState(TicketState.EN_COLA);

        attendedHistory.remove(ticket);

        Node<Ticket> newFront = new Node<>(ticket);
        newFront.next = sourceQueue.getFront();

        sourceQueue.setFront(newFront);
    }

    @Override
    public String toString() {
        return "Finalizar caso";
    }
}