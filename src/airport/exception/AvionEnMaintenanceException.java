package airport.exception;

public class AvionEnMaintenanceException extends RuntimeException {
    public AvionEnMaintenanceException(String immat) {
        super("L'avion " + immat + " est en maintenance.");
    }
}