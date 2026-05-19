package airport.model.avion;

/** CLASSE — Avion : Encapsulation + méthode final */
public class Avion {

    private String  immatriculation;
    private String  modele;
    private int     capacitePassagers;
    private double  autonomieKm;
    private boolean enMaintenance;

    public static final int CAPACITE_MIN = 10;

    public Avion(String immatriculation, String modele,
                 int capacitePassagers, double autonomieKm) {
        this.immatriculation   = immatriculation;
        this.modele            = modele;
        this.capacitePassagers = capacitePassagers;
        this.autonomieKm       = autonomieKm;
        this.enMaintenance     = false;
    }

    // final : ne peut PAS être redéfinie dans une sous-classe
    public final boolean peutEffectuerVol(double distanceKm) {
        return !enMaintenance && autonomieKm >= distanceKm;
    }

    public void mettreEnMaintenance()  { this.enMaintenance = true; }
    public void sortirDeMaintenance()  { this.enMaintenance = false; }

    public String  getImmatriculation()   { return immatriculation; }
    public String  getModele()            { return modele; }
    public int     getCapacitePassagers() { return capacitePassagers; }
    public double  getAutonomieKm()       { return autonomieKm; }
    public boolean isEnMaintenance()      { return enMaintenance; }

    @Override
    public String toString() {
        return immatriculation + " (" + modele + ") — " + capacitePassagers + " places"
               + (enMaintenance ? " [MAINTENANCE]" : "");
    }
}