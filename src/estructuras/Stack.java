/*
 * Implementación de una pila con operaciones básicas.
 * (Push, pop, peek, isEmpty, size)
 * @author Jaime Landázuri
 */

package estructuras;

import java.util.EmptyStackException;

public class Stack<T> {
    private Node<T> top;

    public Stack() {
        this.top = null;
    }

    // Agrega un elemento a la cima de la pila y refiere el nuevo nodo al anterior top
    public void push(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = top;
        top = newNode;
    }

    // Elimina y devuelve el elemento en la cima de la pila y si la pila está vacía, devuelve '\0' (Caracter nulo)
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
       T value = top.value;
        top = top.next;
        return value;
    }

    // Devuelve el elemento en la cima de la pila sin eliminarlo, si la pila está vacía, devuelve '\0' (Caracter nulo)
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.value;
    }

    // Verifica si la pila está vacía
    public boolean isEmpty() {
        return top == null;
    }

    // Devuelve el número de elementos en la pila
    public int size() {
        int count = 0;
        Node<T> current = top;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }
}