package domine;

public enum ProcedureType {
    CERTIFICADO,
    MATRICULA,
    HOMOLOGACION,
    RETIRO_ASIGNATURA,
    OTRO;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}
