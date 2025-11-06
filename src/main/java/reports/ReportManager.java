package reports;

import domine.Ticket;
import domine.TicketState;
import estructures.SimpleList;
import estructures.Node;
import persistence.FileUtils;

/**
 * Esta clase maneja la generación de reportes sobre los tickets.
 * Permite mostrar tickets pendientes por tipo, tickets completados,
 * y los Top-K tickets por número de notas.
 * También permite exportar estos reportes a archivos CSV.
 * @author Wilson Palma
 */
public class ReportManager {

    /**
     * Muestra los tickets pendientes por tipo y opcionalmente exporta CSV.
     */
    public void showPendingByType(SimpleList<Ticket> tickets, boolean exportCsv, String csvPath) {
        csvPath = "data/"+csvPath+".csv";
        StringBuilder console = new StringBuilder();

        console.append("=== Pending Tickets: URGENT ===\n");
        Node<Ticket> current = tickets.head;
        while (current != null) {
            Ticket t = current.value;
            if (t.getState() == TicketState.URGENTE) {
                console.append(formatLine(t)).append("\n");
            }
            current = current.next;
        }

        console.append("\n=== Pending Tickets: NORMAL ===\n");
        current = tickets.head;
        while (current != null) {
            Ticket t = current.value;
            if (t.getState() == TicketState.EN_COLA) {
                console.append(formatLine(t)).append("\n");
            }
            current = current.next;
        }

        // always print to console
        System.out.println(console.toString());

        // export only if user requested
        if (exportCsv) {
            StringBuilder csv = new StringBuilder();
            csv.append("ID;Student;Procedure;State;Notes\n");
            current = tickets.head;
            while (current != null) {
                Ticket t = current.value;
                if (t.getState() == TicketState.URGENTE || t.getState() == TicketState.EN_COLA) {
                    csv.append(formatCsvLine(t)).append("\n");
                }
                current = current.next;
            }
            boolean ok = FileUtils.writeFile(csvPath != null ? csvPath : "pending_by_type.csv", csv.toString());
            System.out.println(ok ? "CSV exported: " + (csvPath != null ? csvPath : "pending_by_type.csv")
                    : "CSV export failed.");
        }
    }

    /**
     * Muestra los tickets completados y opcionalmente exporta CSV.
     */
    public void showCompleted(SimpleList<Ticket> tickets, boolean exportCsv, String csvPath) {
        csvPath = "data/"+csvPath+".csv";
        StringBuilder console = new StringBuilder();
        console.append("=== Completed Tickets ===\n");

        Node<Ticket> current = tickets.head;
        while (current != null) {
            Ticket t = current.value;
            if (t.getState() == TicketState.COMPLETADO) {
                console.append(formatLine(t)).append("\n");
            }
            current = current.next;
        }

        System.out.println(console.toString());

        if (exportCsv) {
            StringBuilder csv = new StringBuilder();
            csv.append("ID;Student;Procedure;FinalState;Notes\n");
            current = tickets.head;
            while (current != null) {
                Ticket t = current.value;
                if (t.getState() == TicketState.COMPLETADO) {
                    csv.append(formatCsvLine(t)).append("\n");
                }
                current = current.next;
            }
            boolean ok = FileUtils.writeFile(csvPath != null ? csvPath : "completed_tickets.csv", csv.toString());
            System.out.println(ok ? "CSV exported: " + (csvPath != null ? csvPath : "completed_tickets.csv")
                    : "CSV export failed.");
        }
    }

    /**
     * Muestra los Top-K tickets por número de notas y opcionalmente exporta CSV.
     */
    public void showTopKByNotes(SimpleList<Ticket> tickets, int k, boolean exportCsv, String csvPath) {
        csvPath = "data/"+csvPath+".csv";
        int n = tickets.size();
        if (n == 0) {
            System.out.println("No tickets available.");
            return;
        }

        // llenar arreglo temporal con tickets
        Ticket[] arr = new Ticket[n];
        Node<Ticket> current = tickets.head;
        int idx = 0;
        while (current != null && idx < n) {
            arr[idx++] = current.value;
            current = current.next;
        }

        // ordenar por selection sort (mayor a menor)
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j].getNoteHistory().size() > arr[maxIdx].getNoteHistory().size()) {
                    maxIdx = j;
                }
            }
            Ticket tmp = arr[i];
            arr[i] = arr[maxIdx];
            arr[maxIdx] = tmp;
        }

        if (k > n) k = n;

        StringBuilder console = new StringBuilder();
        console.append("=== Top ").append(k).append(" Tickets by Notes ===\n");
        for (int i = 0; i < k; i++) {
            Ticket t = arr[i];
            console.append((i + 1)).append(". ").append(t.getStudent())
                    .append(" (ID:").append(t.getId()).append(") - ")
                    .append(t.getNoteHistory().size()).append(" notes\n");
        }

        System.out.println(console.toString());

        if (exportCsv) {
            StringBuilder csv = new StringBuilder();
            csv.append("Rank;ID;Student;Procedure;Notes\n");
            for (int i = 0; i < k; i++) {
                Ticket t = arr[i];
                csv.append((i + 1)).append(";").append(formatCsvLine(t)).append("\n");
            }
            boolean ok = FileUtils.writeFile(csvPath != null ? csvPath : "topk_by_notes.csv", csv.toString());
            System.out.println(ok ? "CSV exported: " + (csvPath != null ? csvPath : "topk_by_notes.csv")
                    : "CSV export failed.");
        }
    }

    // --- helpers ---

    private String formatLine(Ticket t) {
        return "ID:" + t.getId() + " | " + t.getStudent()
                + " | " + t.getProcedureType() + " | " + t.getState()
                + " | Notes:" + t.getNoteHistory().size();
    }

    private String formatCsvLine(Ticket t) {
        return t.getId() + ";" + cleanCsv(t.getStudent()) + ";" + cleanCsv(t.getProcedureType().toString())
                + ";" + t.getState() + ";" + t.getNoteHistory().size();
    }

    private String cleanCsv(String s) {
        if (s == null) return "";
        return s.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }
}