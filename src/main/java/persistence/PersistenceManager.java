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

/**
 * Maneja la persistencia simple en CSV de tickets y notas en el directorio 'data/'.
 * Funcionalidades:
 *  - Guardar y cargar listas de tickets (pendientes e historial)
 *  - Guardar y cargar notas individuales por ticket
 * Implementación orientada a archivos CSV para uso local.
 * @author Alejandro Padilla
 */
public class PersistenceManager {

    private final String basePath = "data/";

    /**
     * Crea el directorio base de persistencia si no existe.
     */
    public PersistenceManager() {
        try {
            Files.createDirectories(Paths.get(basePath));
        } catch (IOException e) {
            System.err.println("Error creating 'data' directory: " + e.getMessage());
        }
    }

    /**
     * Escapa valores para CSV (gestiona comillas y comas internas).
     */
    private String toCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    //funciona para tickets pendientes e historial

    /**
     * Escribe una lista de tickets en un archivo CSV y persiste las notas asociadas.
     */
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

    /**
     * Guarda los tickets pendientes en 'data/pending_tickets.csv'.
     */
    public void saveTickets(SimpleList<Ticket> tickets) {
        saveTicketListToFile(tickets, basePath + "pending_tickets.csv");
    }

    /**
     * Guarda el historial completado en 'data/completed_history.csv'.
     */
    public void saveHistory(SimpleList<Ticket> history) {
        saveTicketListToFile(history, basePath + "completed_history.csv");
    }

    /**
     * Persiste las notas de un ticket en 'data/notes_ticket_<id>.csv'.
     */
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

    /**
     * Carga desde archivos CSV las colas pendientes e historial.
     */
    public void loadData(Queue<Ticket> pendingQueue, Queue<Ticket> historyQueue) {
        loadTicketFile(basePath + "pending_tickets.csv", pendingQueue);
        loadTicketFile(basePath + "completed_history.csv", historyQueue);
    }

    /**
     * Lee un archivo CSV de tickets y encola cada ticket en la cola provista.
     * Ignora líneas malformadas y continúa.
     */
    private void loadTicketFile(String fileName, Queue<Ticket> queue) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Saltar la línea de cabecera
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    java.util.List<String> fields = parseCsvLine(line); // BIEN

                    if (fields.size() < 4) continue; // Línea malformada

                    int id = Integer.parseInt(fields.get(0));
                    String student = fields.get(1); // Ya no necesita .replace("\"", "")
                    ProcedureType procedure = ProcedureType.valueOf(fields.get(2));
                    TicketState state = TicketState.valueOf(fields.get(3));

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

    /**
     * Lee las notas asociadas a un ticket desde su archivo CSV (si existe) y las agrega al historial del ticket.
     */
    private void loadNotesForTicket(Ticket ticket) {
        String noteFile = basePath + "notes_ticket_" + ticket.getId() + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(noteFile))) {
            // Saltar la cabecera del archivo de notas
            reader.readLine();
            String line;

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

    private java.util.List<String> parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Maneja el inicio/fin de comillas
            } else if (c == ',' && !inQuotes) {
                // Es una coma fuera de comillas, fin del campo
                fields.add(currentField.toString());
                currentField.setLength(0); // Limpia para el próximo campo
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString()); // Añade el último campo
        return fields;
    }
}