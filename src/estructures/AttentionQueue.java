
package estructures;

import domine.ProcedureType;
import domine.Ticket;
import java.util.NoSuchElementException;


public class AttentionQueue {

    private Queue<Ticket> normalQueue;
    private Queue<Ticket> urgentQueue;
    private SimpleList<Ticket> attendedHistory;

    public AttentionQueue() {
        this.normalQueue = new Queue<>();
        this.urgentQueue = new Queue<>();
        this.attendedHistory = new SimpleList<>();
    }

    public void addTicket(Ticket t) {
        // The PDF didn't specify, so we use the ProcedureType from the domain
        if (t.getProcedureType().equals(ProcedureType.OTRO.name())) {
            urgentQueue.enqueue(t);
        } else {
            normalQueue.enqueue(t);
        }
    }

    public Ticket nextTicket() {
        if (hasUrgentTickets()) {
            return urgentQueue.peek();   // <-- antes: dequeue()
        }

        if (normalQueue.isEmpty()) {
            throw new NoSuchElementException("No tickets in any queue.");
        }

        return normalQueue.peek();       // <-- antes: dequeue()
    }

    public boolean hasUrgentTickets() {
        return !urgentQueue.isEmpty();
    }

    public void moveToHistory(Ticket t) {
        attendedHistory.pushBack(t);
    }

    public SimpleList<Ticket> getAttendedHistory() {
        return attendedHistory;
    }

    public int getTotalWaiting() {
        return normalQueue.size() + urgentQueue.size();
    }

    public Queue<Ticket> getNormalQueue() {
        return normalQueue;
    }

    public Queue<Ticket> getUrgentQueue() {
        return urgentQueue;
    }
}