package persistence;

import domine.Note;
import domine.ProcedureType;
import domine.Ticket;
import domine.TicketState;
import estructures.Node;
import estructures.Queue;
import estructures.SimpleList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class PersistenceManager {

    private String basePath = "data/";

    public PersistenceManager() {
        try {
            Files.createDirectories(Paths.get(basePath));
        } catch (IOException e) {
            System.err.println("Error creating 'data' directory: " + e.getMessage());
        }
    }

    private String toCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    //funciona para tickets pendientes e historial
    private void saveTicketListToFile(SimpleList<Ticket> tickets, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("id,student,procedure,state");
            writer.newLine();

            Node<Ticket> current = tickets.head;
            while (current != null) {
                Ticket t = current.value;
                String[] fields = {
                        String.valueOf(t.getId()),
                        toCsv(t.getStudent()),
                        toCsv(t.getProcedureType().toString()),
                        toCsv(t.getState().name())
                };
                writer.write(String.join(",", fields));
                writer.newLine();

                saveNotesForTicket(t);
                current = current.next;
            }
        } catch (IOException e) {
            System.err.println("Error saving ticket list to " + filename + ": " + e.getMessage());
        }
    }

    public void saveTickets(SimpleList<Ticket> tickets) {
        saveTicketListToFile(tickets, basePath + "pending_tickets.csv");
    }

    public void saveHistory(SimpleList<Ticket> history) {
        saveTicketListToFile(history, basePath + "completed_history.csv");
    }

    public void saveNotesForTicket(Ticket t) {
        String noteFile = basePath + "notes_ticket_" + t.getId() + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteFile))) {
            writer.write("timestamp,observation");
            writer.newLine();

            Node<Note> current = t.getNoteHistory().head;
            while (current != null) {
                Note note = current.value;
                String[] fields = { toCsv(note.getTimestamp().toString()), toCsv(note.getObservation()) };
                writer.write(String.join(",", fields));
                writer.newLine();
                current = current.next;
            }
        } catch (IOException e) {
            System.err.println("Error saving notes for ticket " + t.getId() + ": " + e.getMessage());
        }
    }

    public void loadData(Queue<Ticket> pendingQueue, Queue<Ticket> historyQueue) {
        loadTicketFile(basePath + "pending_tickets.csv", pendingQueue);
        loadTicketFile(basePath + "completed_history.csv", historyQueue);
    }

    private void loadTicketFile(String fileName, Queue<Ticket> queue) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split(",", 4);
                    int id = Integer.parseInt(fields[0]);
                    String student = fields[1].replace("\"", "");
                    ProcedureType procedure = ProcedureType.valueOf(fields[2].replace("\"", ""));

                    TicketState state = TicketState.valueOf(fields[3].replace("\"", ""));

                    Ticket ticket = new Ticket(student, procedure);
                    ticket.setId(id);
                    ticket.setState(state);

                    loadNotesForTicket(ticket);
                    queue.enqueue(ticket);
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line + ". " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // Normal on first run
        } catch (IOException e) {
            System.err.println("Error reading file " + fileName + ": " + e.getMessage());
        }
    }

    private void loadNotesForTicket(Ticket ticket) {
        String noteFile = basePath + "notes_ticket_" + ticket.getId() + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(noteFile))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split(",", 2);
                    LocalDateTime timestamp = LocalDateTime.parse(fields[0].replace("\"", ""));
                    String observation = fields[1];

                    if (observation.startsWith("\"") && observation.endsWith("\"")) {
                        observation = observation.substring(1, observation.length() - 1);
                        observation = observation.replace("\"\"", "\"");
                    }

                    Note note = new Note(observation, timestamp);
                    ticket.getNoteHistory().pushBack(note);
                } catch (Exception e) {
                    System.err.println("Error processing note line: " + line + ". " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // Normal if ticket had no notes
        } catch (IOException e) {
            System.err.println("Error reading notes for ticket " + ticket.getId() + ": " + e.getMessage());
        }
    }
}