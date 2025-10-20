package domine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note {
    private String observacion;
    private LocalDateTime fecha;

    public Note(String observacion) {
        this.observacion = observacion;
        this.fecha = LocalDateTime.now();
    }

    public String getObservacion() {
        return observacion;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s", fecha.format(formatter), observacion);
    }
}
