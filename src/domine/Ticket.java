package domine;

import estructures.SimpleList;

public class Ticket {
    private int id;
    private String student;
    private ProcedureType procedureType;
    private TicketState state;
    private SimpleList<Note> noteHistory;

    public Ticket(String student, ProcedureType procedureType ) {
        this.student = student;
        this.procedureType = procedureType;
        this.state = TicketState.EN_COLA;
        this.noteHistory = new SimpleList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public SimpleList<Note> getNoteHistory() {
        return noteHistory;
    }

    public String getStudent() {
        return student;
    }

    public ProcedureType getProcedureType() {
        return procedureType;
    }

    public Note agregarNota(String observacion) {
        Note nuevaNota = new Note(observacion);
        noteHistory.pushBack(nuevaNota);
        return nuevaNota;
    }
}
