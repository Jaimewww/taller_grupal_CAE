package util;

import domine.Ticket;
import domine.TicketState;

import java.util.*;
import java.util.function.Consumer;

/**
 * StateMachine para validar y describir transiciones entre TicketState.
 *
 * - Contiene reglas por defecto (según el diseño del dominio).
 * - Permite agregar/quitar transiciones y asociar descripciones opcionales.
 * - Thread-safe.
 */
public final class StateMachine {

    /** Mapa de transiciones: estado origen -> conjunto de estados destino permitidos */
    private final Map<TicketState, EnumSet<TicketState>> transitions;

    /** Map de descripciones opcionales por transición */
    private final Map<Transition, String> descriptions;

    /** Mutex para concurrencia */
    private final Object lock = new Object();

    /** Construye la máquina con las reglas por defecto definidas en el diseño. */
    public StateMachine() {
        this.transitions = new EnumMap<>(TicketState.class);
        this.descriptions = new HashMap<>();
        initializeEmptyTransitions();
        loadDefaultRules();
    }

    /** Inicializa las entradas del EnumMap para todos los valores del enum (con conjuntos vacíos). */
    private void initializeEmptyTransitions() {
        for (TicketState s : TicketState.values()) {
            transitions.put(s, EnumSet.noneOf(TicketState.class));
        }
    }

    /** Carga las reglas sugeridas en el markdown. */
    private void loadDefaultRules() {
        // EN_COLA -> EN_ATENCION, URGENTE, PENDIENTE_DOCS
        addTransition(TicketState.EN_COLA, TicketState.EN_ATENCION,
                "Del estado en cola se puede pasar a atención, marcar urgente o solicitar docs");
        addTransition(TicketState.EN_COLA, TicketState.URGENTE,
                "Promover a urgente desde la cola");
        addTransition(TicketState.EN_COLA, TicketState.PENDIENTE_DOCS,
                "Marcar como pendiente de documentos desde la cola");

        // URGENTE -> EN_ATENCION, PENDIENTE_DOCS
        addTransition(TicketState.URGENTE, TicketState.EN_ATENCION,
                "Atender un ticket urgente");
        addTransition(TicketState.URGENTE, TicketState.PENDIENTE_DOCS,
                "Pendiente de documentos mientras es urgente");

        // EN_ATENCION -> COMPLETADO, PENDIENTE_DOCS
        addTransition(TicketState.EN_ATENCION, TicketState.COMPLETADO,
                "Finalizar atención");
        addTransition(TicketState.EN_ATENCION, TicketState.PENDIENTE_DOCS,
                "Solicitar documentos durante la atención");

        // PENDIENTE_DOCS -> EN_COLA, EN_ATENCION
        addTransition(TicketState.PENDIENTE_DOCS, TicketState.EN_COLA,
                "Volver a la cola una vez completados los documentos");
        addTransition(TicketState.PENDIENTE_DOCS, TicketState.EN_ATENCION,
                "Reanudar atención tras recibir documentos");

        // COMPLETADO -> (no transiciones)
        // se deja explícitamente vacío
    }

    /**
     * Agrega (o actualiza) una transición válida.
     *
     * @param from        estado origen
     * @param to          estado destino
     * @param description (opcional) descripción humana de la transición; puede ser null
     */
    public void addTransition(TicketState from, TicketState to, String description) {
        Objects.requireNonNull(from, "from no puede ser null");
        Objects.requireNonNull(to, "to no puede ser null");
        synchronized (lock) {
            EnumSet<TicketState> set = transitions.computeIfAbsent(from, k -> EnumSet.noneOf(TicketState.class));
            set.add(to);
            if (description != null && !description.trim().isEmpty()) {
                descriptions.put(new Transition(from, to), description.trim());
            }
        }
    }

    /**
     * Quita una transición (si existe).
     *
     * @param from estado origen
     * @param to   estado destino
     * @return true si la transición existía y fue removida
     */
    public boolean removeTransition(TicketState from, TicketState to) {
        Objects.requireNonNull(from, "from no puede ser null");
        Objects.requireNonNull(to, "to no puede ser null");
        synchronized (lock) {
            EnumSet<TicketState> set = transitions.get(from);
            if (set == null || !set.contains(to)) return false;
            set.remove(to);
            descriptions.remove(new Transition(from, to));
            return true;
        }
    }

    /**
     * Verifica si una transición es válida según las reglas actuales.
     *
     * @param from estado origen
     * @param to   estado destino
     * @return true si la transición está permitida
     */
    public boolean isValidTransition(TicketState from, TicketState to) {
        Objects.requireNonNull(from, "from no puede ser null");
        Objects.requireNonNull(to, "to no puede ser null");
        synchronized (lock) {
            EnumSet<TicketState> set = transitions.get(from);
            return set != null && set.contains(to);
        }
    }

    /**
     * Devuelve una lista (inmutable) con los estados permitidos desde el estado dado.
     *
     * @param from estado origen
     * @return lista ordenada de estados destino permitidos (vacía si no hay)
     */
    public List<TicketState> allowedNextStates(TicketState from) {
        Objects.requireNonNull(from, "from no puede ser null");
        synchronized (lock) {
            EnumSet<TicketState> set = transitions.get(from);
            if (set == null || set.isEmpty()) return Collections.emptyList();
            List<TicketState> copy = new ArrayList<>(set);
            // mantener orden estable (el orden del enum)
            copy.sort(Comparator.comparingInt(Enum::ordinal));
            return Collections.unmodifiableList(copy);
        }
    }

    /**
     * Devuelve una descripción humana de la transición. Si hay una descripción personalizada la retorna;
     * si no, devuelve un mensaje estándar o indica que no está permitida.
     *
     * @param from estado origen
     * @param to   estado destino
     * @return cadena descriptiva
     */
    public String describeTransition(TicketState from, TicketState to) {
        Objects.requireNonNull(from, "from no puede ser null");
        Objects.requireNonNull(to, "to no puede ser null");
        synchronized (lock) {
            Transition t = new Transition(from, to);
            if (!isValidTransition(from, to)) {
                return String.format("Transición NO permitida: %s -> %s", from, to);
            }
            String custom = descriptions.get(t);
            if (custom != null && !custom.isEmpty()) return custom;
            // mensaje por defecto si no hay custom
            return String.format("Transición permitida: %s -> %s", from, to);
        }
    }

    /**
     * Intenta aplicar la transición al Ticket (cambiando su estado) si es válida.
     * Este método solo valida y aplica en memoria (no persiste). Devuelve true si se aplicó.
     *
     * @param ticket  ticket objetivo (no null)
     * @param to      nuevo estado
     * @param onError callback (opcional) que recibe un mensaje en caso de error; puede ser null
     * @return true si la transición fue válida y se aplicó; false en caso contrario
     */
    public boolean applyIfValid(Ticket ticket, TicketState to, Consumer<String> onError) {
        Objects.requireNonNull(ticket, "ticket no puede ser null");
        Objects.requireNonNull(to, "to no puede ser null");
        TicketState from = ticket.getState();
        synchronized (lock) {
            if (!isValidTransition(from, to)) {
                if (onError != null) {
                    onError.accept(String.format("Transición inválida: %s -> %s", from, to));
                }
                return false;
            }
            // aplicar
            ticket.setState(to);
            return true;
        }
    }

    /**
     * Devuelve todas las transiciones actuales como pares (from -> Set(to)).
     * Retorna un Map copia para evitar exponer la estructura interna.
     */
    public Map<TicketState, Set<TicketState>> getAllTransitions() {
        synchronized (lock) {
            Map<TicketState, Set<TicketState>> copy = new EnumMap<>(TicketState.class);
            for (Map.Entry<TicketState, EnumSet<TicketState>> e : transitions.entrySet()) {
                copy.put(e.getKey(), EnumSet.copyOf(e.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    /**
     * Resetea las reglas actuales y carga las reglas por defecto nuevamente.
     */
    public void resetToDefaults() {
        synchronized (lock) {
            transitions.clear();
            descriptions.clear();
            initializeEmptyTransitions();
            loadDefaultRules();
        }
    }

    /* ------------------ helpers / inner classes ------------------ */

    /**
     * Representa una transición (pair: from -> to) para usar como key en el mapa de descripciones.
     */
    private static final class Transition {
        private final TicketState from;
        private final TicketState to;

        Transition(TicketState from, TicketState to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transition)) return false;
            Transition that = (Transition) o;
            return from == that.from && to == that.to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    /* ------------------ Ejemplo de uso en javadoc / comments ------------------
       StateMachine sm = new StateMachine();
       boolean ok = sm.isValidTransition(TicketState.EN_COLA, TicketState.EN_ATENCION);
       List<TicketState> next = sm.allowedNextStates(TicketState.PENDIENTE_DOCS);
       sm.addTransition(TicketState.EN_COLA, TicketState.COMPLETADO, "Atajo excepcional"); // si necesario
       sm.resetToDefaults(); // vuelve al estado inicial
    --------------------------------------------------------------------------- */
}
