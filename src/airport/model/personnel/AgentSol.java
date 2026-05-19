package airport.model.personnel;

import java.util.ArrayList;
import java.util.List;

/** CLASSE — AgentSol : Héritage + Interface Notifiable */
public class AgentSol extends Personne implements Notifiable {

    private String secteur;
    private int    experience;
    private List<String> notifications = new ArrayList<>();

    public static final String[] SECTEURS_VALIDES = {
        "Enregistrement", "Bagages", "Sécurité", "Embarquement"
    };

    public AgentSol(String id, String nom, String prenom,
                    String email, String secteur, int experience) {
        super(id, nom, prenom, email);
        this.secteur    = secteur;
        this.experience = experience;
    }

    @Override public String getRole() { return "Agent Sol (" + secteur + ")"; }

    @Override public void recevoirNotification(String message) { recevoirNotification(message, 1); }
    @Override public void recevoirNotification(String message, int priorite) {
        notifications.add((priorite >= 3 ? "🚨 " : "ℹ ") + message);
    }

    public boolean peutSuperviser() { return experience >= 3; }
    public String  getSecteur()     { return secteur; }
    public int     getExperience()  { return experience; }
}