package controller.command;

import controller.IAction;
import domine.Note;
import domine.Ticket;

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
}
