package controller.command;

import controller.IAction;
import domine.Note;
import domine.Ticket;


/**
* Esta clase representa un comando para agregar una nota a un ticket.
* Implementa la interfaz IAction, que define los métodos execute y undo.
* @author Jaime Landázuri
* */

public class AddNoteCommand implements IAction {
    private Ticket ticket;
    private Note note;

    public AddNoteCommand(Ticket ticket, Note note) {
        this.ticket = ticket;
        this.note = note;
    }

    @Override
    public void execute() {
        ticket.getNoteHistory().pushBack(note);
    }

    @Override
    public void undo() {
        ticket.getNoteHistory().remove(note);
    }

    @Override
    public String toString() {
        return "Agregar nota";
    }
}
