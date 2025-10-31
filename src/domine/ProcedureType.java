package domine;

public enum ProcedureType {
    CERTIFICADO,
    MATRÍCULA,
    HOMOLOGACIÓN,
    RETIRO_ASIGNATURA,
    OTRO;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}
