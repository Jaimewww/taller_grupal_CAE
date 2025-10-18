package dominio;

import estructuras.SimpleList;

public class Ticket {
    private int id;
    private EstadoTicket estado;
    private SimpleList<Nota> historialNotas;

    public Ticket(int id, EstadoTicket estado) {
        this.id = id;
        this.estado = estado;
        this.historialNotas = new SimpleList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EstadoTicket getEstado() {
        return estado;
    }

    public void setEstado(EstadoTicket estado) {
        this.estado = estado;
    }

    public SimpleList<Nota> getHistorialNotas() {
        return historialNotas;
    }

    public void setHistorialNotas(SimpleList<Nota> historialNotas) {
        this.historialNotas = historialNotas;
    }

    public Nota agregarNota(String observacion) {
        Nota nuevaNota = new Nota(observacion);
        historialNotas.pushBack(nuevaNota);
        return nuevaNota;
    }
}
