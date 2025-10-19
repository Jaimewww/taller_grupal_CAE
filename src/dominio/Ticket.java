package dominio;

import estructuras.SimpleList;

public class Ticket {
    private int id;
    private String estudiante;
    private String tipoTramite;
    private EstadoTicket estado;
    private SimpleList<Nota> historialNotas;

    public Ticket(String estudiante, String tipoTramite ) {
        this.estudiante = estudiante;
        this.tipoTramite = tipoTramite;
        this.estado = EstadoTicket.EN_COLA;
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

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public Nota agregarNota(String observacion) {
        Nota nuevaNota = new Nota(observacion);
        historialNotas.pushBack(nuevaNota);
        return nuevaNota;
    }
}
