package estructures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

class EstructuresTest {

    // --- SimpleList<T> ---

    @Test
    @DisplayName("SimpleList: Falla al remover objeto por contenido (Bug ==)")
    void simpleListRemoveFailsWithEqualsBug() {
        SimpleList<String> list = new SimpleList<>();
        list.pushBack("Hello");

        String keyToRemove = new String("Hello"); // Mismo contenido, DIFERENTE objeto

        assertDoesNotThrow(() -> {
            String removed = list.remove(keyToRemove);
            assertEquals("Hello", removed);
        }, "Falla porque SimpleList.remove() usa '==' en lugar de '.equals()'");

    }

    @Test
    @DisplayName("SimpleList: Falla al buscar objeto por contenido (Bug ==)")
    void simpleListFindFailsWithEqualsBug() {
        SimpleList<String> list = new SimpleList<>();
        list.pushBack("World");
        String keyToFind = new String("World");

        assertDoesNotThrow(() -> {
            String found = list.find(keyToFind);
            assertEquals("World", found);
        }, "Falla porque SimpleList.find() usa '==' en lugar de '.equals()'");
    }

    // --- Queue<T> ---

    @Test
    @DisplayName("Queue: Correcto: encuentra y remueve por contenido (.equals())")
    void queueFindAndRemoveWorksCorrectly() {
        Queue<String> q = new Queue<>();
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");

        String keyToFind = new String("B");
        String keyToRemove = new String("C");

        // Estas pruebas SÍ PASAN porque Queue.java usa .equals() correctamente
        assertDoesNotThrow(() -> {
            assertEquals("B", q.find(keyToFind));
            assertEquals("C", q.remove(keyToRemove));
        });
        assertEquals(2, q.size());
        assertEquals("A", q.dequeue());
        assertEquals("B", q.dequeue());
        assertTrue(q.isEmpty());
    }

    @Test
    @DisplayName("Queue: Lanza excepción al hacer dequeue en cola vacía")
    void queueThrowsOnEmptyDequeue() {
        Queue<Integer> q = new Queue<>();
        assertThrows(NoSuchElementException.class, q::dequeue);
    }

    // --- Stack<T> ---

    @Test
    @DisplayName("Stack: LIFO (Push, Pop, Peek)")
    void stackLIFOLogic() {
        Stack<Integer> stack = new Stack<>();
        stack.push(10);
        stack.push(20);

        assertEquals(2, stack.size());
        assertEquals(20, stack.peek());
        assertEquals(20, stack.pop());
        assertEquals(10, stack.peek());
        assertEquals(1, stack.size());

        stack.pop();
        assertTrue(stack.isEmpty());
    }

    @Test
    @DisplayName("Stack: Lanza excepción al hacer pop en pila vacía")
    void stackThrowsOnEmptyPop() {
        Stack<Integer> stack = new Stack<>();
        assertThrows(EmptyStackException.class, stack::pop);
    }
}