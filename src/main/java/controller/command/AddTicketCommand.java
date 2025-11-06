package controller.command;

import controller.IAction;
import domine.Ticket;
import estructures.Queue;

/**
 * Esta clase representa un comando para agregar un ticket a una cola de tickets.
 * Implementa la interfaz IAction, que define los métodos execute y undo.
 * @author Jaime Landázuri
 * */

public class AddTicketCommand implements IAction {
    private Queue<Ticket> ticketQueue;
    private Ticket newTicket;

    public AddTicketCommand(Queue<Ticket> ticketQueue, Ticket newTicket) {
        this.ticketQueue = ticketQueue;
        this.newTicket = newTicket;
    }

    @Override
    public void execute() {
        ticketQueue.enqueue(newTicket);
    }

    @Override
    public void undo() {
        Queue<Ticket> tempQueue = new Queue<>();
        while (!ticketQueue.isEmpty()) {
            Ticket ticket = ticketQueue.dequeue();
            if (ticket != newTicket) {
                tempQueue.enqueue(ticket);
            }
        }
        while (!tempQueue.isEmpty()) {
            ticketQueue.enqueue(tempQueue.dequeue());
        }
    }

    @Override
    public String toString() {
        return "Agregar ticket";
    }
}
