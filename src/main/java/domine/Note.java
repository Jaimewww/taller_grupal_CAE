package domine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Esta clase representa una nota asociada a un ticket.
 * Contiene una observación y una marca de tiempo.
 * @author Jaime Landázuri
 */

public class Note {
    private String observation;
    private LocalDateTime timestamp;

    public Note(String observation) {
        this.observation = observation;
        this.timestamp = LocalDateTime.now();
    }

    public Note(String observation, LocalDateTime timestamp) {
        this.observation = observation;
        this.timestamp = timestamp;
    }

    public String getObservation() {
        return observation;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s", timestamp.format(formatter), observation);
    }
}