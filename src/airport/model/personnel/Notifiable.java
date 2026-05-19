package airport.model.personnel;

/** INTERFACE — Notifiable : contrat de notification (surcharge) */
public interface Notifiable {
    void recevoirNotification(String message);
    void recevoirNotification(String message, int priorite); // surcharge
}