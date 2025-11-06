package estructures;

import java.util.NoSuchElementException;

/**
 * Clase que representa una cola (FIFO - First In First Out).
 * @param <T> Tipo de dato que almacena la cola.
 * @author Jaime Landázuri
 * */

public class Queue<T> {
    private Node<T> front;
    private Node<T> rear;

    public Queue() {
        this.front = null;
        this.rear = null;
    }

    // Agrega un elemento al final de la cola
    public void enqueue(T value) {
        Node<T> newNode = new Node<>(value);
        if (rear != null) {
            rear.next = newNode;
        }
        rear = newNode;
        if (front == null) {
            front = newNode;
        }
    }

    // Elimina y devuelve el elemento al frente de la cola, si la cola está vacía, devuelve -1
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T value = front.value;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        return value;
    }

    // Devuelve el elemento al frente de la cola sin eliminarlo, si la cola está vacía, devuelve -1
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return front.value;
    }

    // Verifica si la cola está vacía
    public boolean isEmpty() {
        return front == null;
    }

    // Devuelve el número de elementos en la cola
    public int size() {
        int count = 0;
        Node<T> current = front;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    public T find(T key){
        Node<T> current = front;
        while(current != null){
            if(current.value.equals(key)){
                return current.value;
            }
            current = current.next;
        }
        throw new NoSuchElementException();
    }

    public T remove(T key){
        Node<T> current = front;
        Node<T> prev = null;

        // Si el nodo a eliminar es el front
        if(current != null && current.value.equals(key)){
            front = current.next;
            if(front == null){
                rear = null;
            }
            return current.value;
        }

        while(current != null && !current.value.equals(key)){
            prev = current;
            current = current.next;
        }

        if(current == null){
            throw new NoSuchElementException();
        }

        prev.next = current.next;
        if(current == rear){
            rear = prev;
        }
        return current.value;
    }

    public Node<T> getFront() {
        return front;
    }

    public void setFront(Node<T> front) {
        this.front = front;
    }
}