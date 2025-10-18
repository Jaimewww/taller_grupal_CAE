import dominio.Nota;
import dominio.Ticket;
import estructuras.Queue;
import estructuras.SimpleList;

public class Main {
    public static void main(String[] args) {
        Queue<Ticket> ticketQueue = new Queue<>();
        Ticket newTicket = new Ticket(1, null);
        Ticket ticket1 = new Ticket(5, null);
        ticketQueue.enqueue(newTicket);
        Nota newNota = ticket1.agregarNota("wilson");
        SimpleList<Nota> historial = ticket1.getHistorialNotas();
        System.out.println(historial.find(newNota).toString());
    }
}