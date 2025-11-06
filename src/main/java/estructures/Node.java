package estructures;
/**
 * Clase que representa un nodo en una estructura de datos enlazada.
 * @param <T> Tipo de dato que almacena el nodo.
 * @author Jaime Landázuri
 * */

public class Node<T> {
    public T value;
    public Node<T> next;

    public Node(T value) {
        this.value = value;
        this.next = null;
    }
}
