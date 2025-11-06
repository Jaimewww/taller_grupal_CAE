package controller;
/*
* Esta interfaz define las acciones que pueden ser ejecutadas y deshechas.
* Cualquier comando que implemente esta interfaz debe proporcionar
* implementaciones para los métodos execute y undo.
* @author Jaime Landázuri
 */
public interface IAction {
    void execute();
    void undo();
}
