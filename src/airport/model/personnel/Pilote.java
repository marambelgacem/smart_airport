package airport.model.personnel;

import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE — Pilote
 * Concept POO : Héritage + 2 Interfaces + Polymorphisme + final
 */
public class Pilote extends Personne implements Authentifiable, Notifiable {

    private String licencePilote;
    private int    heuresDeVol;
    private String motDePasse;
    private boolean connecte;
    private List<String> notifications = new ArrayList<>();

    public static final int HEURES_MIN_LONG_COURRIER = 1000;

    public Pilote(String id, String nom, String prenom, String email,
                  String licencePilote, int heuresDeVol, String motDePasse) {
        super(id, nom, prenom, email);
        this.licencePilote = licencePilote;
        this.heuresDeVol   = heuresDeVol;
        this.motDePasse    = motDePasse;
        this.connecte      = false;
    }

    @Override public String getRole() { return "Pilote"; }

    @Override
    public boolean seConnecter(String motDePasse) {
        if (this.motDePasse.equals(motDePasse)) { this.connecte = true; return true; }
        return false;
    }
    @Override public void seDeconnecter() { this.connecte = false; }
    @Override public boolean estConnecte() { return connecte; }

    @Override
    public void recevoirNotification(String message) { recevoirNotification(message, 1); }

    @Override
    public void recevoirNotification(String message, int priorite) {
        String prefixe = priorite >= 3 ? "🚨 URGENT" : priorite == 2 ? "⚠ ALERTE" : "ℹ INFO";
        notifications.add(prefixe + " → " + message);
    }

    public boolean estQualifieLongCourrier() { return heuresDeVol >= HEURES_MIN_LONG_COURRIER; }
    public void ajouterHeuresVol(int h) { if (h > 0) heuresDeVol += h; }

    public String  getLicencePilote()       { return licencePilote; }
    public int     getHeuresDeVol()         { return heuresDeVol; }
    public List<String> getNotifications()  { return notifications; }
}