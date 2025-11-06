package controller;

import estructures.Stack;

/**
    Clase que gestiona las acciones realizadas en la aplicacion,
    permitiendo deshacer y rehacer acciones.
    se utilizan dos pilas: una para las acciones deshechas (undoStack)
    y otra para las acciones rehechas (redoStack).
    @author Jaime Landázuri
*/

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

    public Stack<IAction> getUndoStack() {
        return undoStack;
    }

    public Stack<IAction> getRedoStack() {
        return redoStack;
    }

}
