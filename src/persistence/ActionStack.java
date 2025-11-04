
package persistence;

import domine.Action;
import estructures.Stack;
import java.util.EmptyStackException;

public class ActionStack {

    private Stack<Action> undoStack;
    private Stack<Action> redoStack;

    public ActionStack() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void registerAction(Action a) {
        undoStack.push(a);
        clearRedo();
    }


    public void undo() {
        if (undoStack.isEmpty()) {
            throw new EmptyStackException();
        }

        Action a = undoStack.pop();
        a.undo();
        redoStack.push(a);
    }



    public void redo() {
        if (redoStack.isEmpty()) {
            throw new EmptyStackException(); // Throws your Stack's exception
        }

        Action a = redoStack.pop();
        a.execute();
        undoStack.push(a);
    }


    public void clearRedo() {
        redoStack = new Stack<>();
    }

    public boolean isUndoEmpty() {
        return undoStack.isEmpty();
    }

    public boolean isRedoEmpty() {
        return redoStack.isEmpty();
    }
}