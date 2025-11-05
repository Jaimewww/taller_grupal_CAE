package controller.command;

import controller.IAction;
import domine.Ticket;
import domine.TicketState;
import estructures.Queue;

public class HandleCaseCommand implements IAction {
    private Ticket currentTicket;
    private TicketState previousState;
    private Queue<Ticket> originQueue;

    private Queue<Ticket> sourceQueue;
    private Queue<Ticket> urgentQueue;

    public HandleCaseCommand(Queue<Ticket> sourceQueue, Queue<Ticket> urgentQueue) {
        this.sourceQueue = sourceQueue;
        this.urgentQueue = urgentQueue;
    }

    @Override
    public void execute() {
        if (!urgentQueue.isEmpty()) {
            this.originQueue = urgentQueue;
        } else if (!sourceQueue.isEmpty()) {
            this.originQueue = sourceQueue;
        } else {
            this.currentTicket = null;
            this.originQueue = null;
            return;
        }

        this.currentTicket = this.originQueue.dequeue();
        this.previousState = this.currentTicket.getState();
        
        this.currentTicket.setState(TicketState.EN_ATENCION);
    }

    @Override
    public void undo() {
        if (this.currentTicket == null || this.originQueue == null) {
            return;
        }

        this.currentTicket.setState(this.previousState);

        this.originQueue.enqueue(this.currentTicket);

        this.currentTicket = null;
        this.previousState = null;
        this.originQueue = null;
    }
}