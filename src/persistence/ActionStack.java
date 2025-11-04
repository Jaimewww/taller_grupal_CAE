
package persistence;

import controller.IAction;
import estructures.Stack;
import java.util.EmptyStackException;

public class ActionStack {

    private Stack<IAction> undoStack;
    private Stack<IAction> redoStack;

    public ActionStack() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void registerAction(IAction a) {
        undoStack.push(a);
        clearRedo();
    }


    public void undo() {
        if (undoStack.isEmpty()) {
            throw new EmptyStackException();
        }

        IAction a = undoStack.pop();
        a.undo();
        redoStack.push(a);
    }



    public void redo() {
        if (redoStack.isEmpty()) {
            throw new EmptyStackException(); // Throws your Stack's exception
        }

        IAction a = redoStack.pop();
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