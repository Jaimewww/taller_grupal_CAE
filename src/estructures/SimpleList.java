/*
 * Implementación de una lista enlazada simple con operaciones básicas.
 * (pushFront, pushBack, find, remove, size, isEmpty, clear)
 * @author Jaime Landázuri
 */
package estructures;

import java.util.NoSuchElementException;

public class SimpleList<T> {
    public Node<T> head;

    public SimpleList() {
        this.head = null;
    }

    // Inserta nodo al inicio de la lista
    public void pushFront(T newData){
        Node<T> newNode = new Node<>(newData);
        newNode.next = head;
        head = newNode;
    }

    // Inserta nodo al final de la lista
    public void pushBack(T newData){
        Node<T> newNode = new Node<>(newData);
        if(head == null){
            head = newNode;
            return;
        }
        Node<T> last = head;
        while(last.next != null){
            last = last.next;
        }
        last.next = newNode;
    }

    // Busca un nodo por su valor y devuelve el valor si lo encuentra, -1 si no
    public T find(T key){
        Node<T> current = head;
        while(current != null){
            if(current.value == key){
                return current.value;
            }
            current = current.next;
        }
        throw new NoSuchElementException();
    }

    // Elimina un nodo por su valor y devuelve el valor si lo encuentra, -1 si no
    public T remove(T key){
        Node<T> current = head;
        Node<T> prev = null;

        // Si el nodo a eliminar es el head
        if(current != null && current.value == key){
            head = current.next;
            return current.value;
        }

        while(current != null && current.value != key){
            prev = current;
            current = current.next;
        }

        if(current == null){
            throw new NoSuchElementException();
        }

        prev.next = current.next;
        return current.value;
    }

    // Devuelve el tamaño de la lista haciendo un recorrido y conteo
    public int size(){
        int count = 0;
        Node<T> current = head;
        while(current != null){
            count++;
            current = current.next;
        }
        return count;
    }

    // Verifica si la lista está vacía
    public boolean isEmpty(){
        return head == null;
    }

    // Vacía la lista haciendola apuntar a null
    public void clear(){
        head = null;
    }
}