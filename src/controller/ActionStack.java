package controller;

import estructures.Stack;

public class ActionStack {
    private Stack<IAction> undoStack = new Stack<>();
    private Stack<IAction> redoStack = new Stack<>();

    public void registerAction(IAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;

        IAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        IAction action = redoStack.pop();
        action.execute();
        undoStack.push(action);
    }

    // Devuelve true si no hay acciones para deshacer
    public boolean isEmpty() {
        return undoStack.isEmpty();
    }

    // Nuevo: indica si hay acciones para rehacer
    public boolean hasRedo() {
        return !redoStack.isEmpty();
    }

    public Stack<IAction> getUndoStack() {
        return undoStack;
    }

    public void setUndoStack(Stack<IAction> undoStack) {
        this.undoStack = undoStack;
    }

    public Stack<IAction> getRedoStack() {
        return redoStack;
    }

    public void setRedoStack(Stack<IAction> redoStack) {
        this.redoStack = redoStack;
    }
}
