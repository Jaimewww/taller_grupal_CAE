
package domine;

import java.util.NoSuchElementException;

public class AddNoteAction implements Action {

    private Ticket ticket;
    private Note note;

    public AddNoteAction(Ticket ticket, Note note) {
        this.ticket = ticket;
        this.note = note;
    }

    @Override
    public void execute() {
        ticket.getNoteHistory().pushBack(note);
    }

    @Override
    public void undo() {
        try {
            ticket.getNoteHistory().remove(note);
        } catch (NoSuchElementException e) {
            // This could happen if the list was modified externally.
            System.err.println("Error while undoing: could not remove the note. " + e.getMessage());
        }
    }
}