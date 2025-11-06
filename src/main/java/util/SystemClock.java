package util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Esta clase proporciona una envoltura alrededor de java.time.Clock
 * para facilitar la obtención de la hora actual y permitir la
 * inyección de relojes personalizados (útil para pruebas).
 * Permite obtener la hora actual en diferentes formatos,
 * formatear fechas y modificar el reloj subyacente.
 *   @author Wilson Palma
 */
public final class SystemClock {

    /** Reloj subyacente. Volatile para lecturas concurrentes rápidas. */
    private volatile Clock clock;

    /* ------------------ Constructores ------------------ */

    /** Crea un SystemClock con el reloj del sistema en la zona por defecto. */
    public SystemClock() {
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Crea un SystemClock con la zona especificada (usa Clock.system(zone)).
     *
     * @param zone zona a usar (no null)
     */
    public SystemClock(ZoneId zone) {
        Objects.requireNonNull(zone, "zone no puede ser null");
        this.clock = Clock.system(zone);
    }

    /**
     * Crea un SystemClock con un Clock ya provisto (útil para inyección).
     *
     * @param clock reloj a usar (no null)
     */
    public SystemClock(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock no puede ser null");
    }

    /* ------------------ Lectura del tiempo ------------------ */

    /**
     * Devuelve la fecha y hora actual como LocalDateTime (según el clock actual).
     *
     * @return LocalDateTime actual
     */
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    /**
     * Devuelve la fecha y hora actual como ZonedDateTime (según el clock actual,
     * incluyendo la zona del clock).
     *
     * @return ZonedDateTime actual
     */
    public ZonedDateTime nowZoned() {
        return ZonedDateTime.now(clock);
    }

    /**
     * Devuelve la fecha y hora actual en la zona indicada.
     *
     * @param zone zona deseada (no null)
     * @return ZonedDateTime actual en la zona indicada
     */
    public ZonedDateTime now(ZoneId zone) {
        Objects.requireNonNull(zone, "zone no puede ser null");
        return ZonedDateTime.now(clock.withZone(zone));
    }

    /* ------------------ Formateo ------------------ */

    /**
     * Formatea el tiempo actual (ZonedDateTime del clock actual) con el patrón dado.
     * Usa {@link DateTimeFormatter#ofPattern(String)} y lanza RuntimeException si el patrón es inválido.
     *
     * @param pattern patrón con el que formatear (por ejemplo "yyyy-MM-dd HH:mm:ss")
     * @return cadena formateada
     */
    public String nowFormatted(String pattern) {
        Objects.requireNonNull(pattern, "pattern no puede ser null");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return fmt.format(nowZoned());
    }

    /**
     * Formatea un LocalDateTime con el patrón dado.
     *
     * @param dateTime LocalDateTime a formatear (no null)
     * @param pattern  patrón (no null)
     * @return cadena con el formato solicitado
     */
    public String format(LocalDateTime dateTime, String pattern) {
        Objects.requireNonNull(dateTime, "dateTime no puede ser null");
        Objects.requireNonNull(pattern, "pattern no puede ser null");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return fmt.format(dateTime);
    }

    /**
     * Formatea un ZonedDateTime con el patrón dado.
     *
     * @param zonedDateTime ZonedDateTime a formatear (no null)
     * @param pattern       patrón (no null)
     * @return cadena con el formato solicitado
     */
    public String format(ZonedDateTime zonedDateTime, String pattern) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime no puede ser null");
        Objects.requireNonNull(pattern, "pattern no puede ser null");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return fmt.format(zonedDateTime);
    }

    /* ------------------ Modificación del clock (útil para pruebas) ------------------ */

    /**
     * Fija el reloj a un instante (Instant) en la zona indicada.
     * Usar para pruebas deterministas.
     *
     * @param instant instante a fijar (no null)
     * @param zone    zona (no null)
     */
    public synchronized void setFixed(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant no puede ser null");
        Objects.requireNonNull(zone, "zone no puede ser null");
        this.clock = Clock.fixed(instant, zone);
    }

    /**
     * Fija el reloj usando un LocalDateTime y una ZoneId (convierte a Instant internamente).
     *
     * @param localDateTime fecha/hora local (no null)
     * @param zone          zona (no null)
     */
    public synchronized void setFixed(LocalDateTime localDateTime, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime no puede ser null");
        Objects.requireNonNull(zone, "zone no puede ser null");
        Instant instant = localDateTime.atZone(zone).toInstant();
        this.clock = Clock.fixed(instant, zone);
    }

    /**
     * Establece un clock con un offset respecto al reloj del sistema.
     *
     * @param offset duración de offset (puede ser positiva o negativa; no null)
     */
    public synchronized void setOffset(Duration offset) {
        Objects.requireNonNull(offset, "offset no puede ser null");
        this.clock = Clock.offset(Clock.systemDefaultZone(), offset);
    }

    /**
     * Resetea el clock a comportamiento por defecto (reloj del sistema con zone por defecto).
     */
    public synchronized void resetToSystemDefault() {
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Reemplaza el clock actual por uno provisto.
     *
     * @param newClock nuevo Clock (no null)
     */
    public synchronized void setClock(Clock newClock) {
        this.clock = Objects.requireNonNull(newClock, "newClock no puede ser null");
    }

    /* ------------------ Helpers / accessors ------------------ */

    /**
     * Obtiene el Clock actual. {@link Clock} es inmutable por diseño, por lo que
     * devolver la referencia es seguro (pero el campo puede cambiar por setters).
     *
     * @return Clock actual
     */
    public Clock getClock() {
        return clock;
    }

    @Override
    public String toString() {
        return "SystemClock{" + "clock=" + clock + '}';
    }
}