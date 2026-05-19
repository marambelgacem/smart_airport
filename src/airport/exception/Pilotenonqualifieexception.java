package airport.exception;

public class PiloteNonQualifieException extends Exception {
    private final String idPilote;
    public PiloteNonQualifieException(String idPilote, String raison) {
        super("Pilote " + idPilote + " non qualifié : " + raison);
        this.idPilote = idPilote;
    }
    public String getIdPilote() { return idPilote; }
}