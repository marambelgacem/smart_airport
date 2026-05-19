package airport.model.vol;

import airport.model.avion.Avion;
import airport.model.personnel.Passager;
import airport.model.personnel.Pilote;
import airport.exception.VolCompletException;
import airport.exception.VolIndisponibleException;

import java.util.ArrayList;
import java.util.List;

/** CLASSE — Vol : Collections List + Exceptions personnalisées */
public class Vol {

    private String     numeroVol;
    private String     villeDepart;
    private String     villeArrivee;
    private double     distanceKm;
    private StatutVol  statut;
    private Avion      avion;
    private Pilote     pilote;
    private List<Passager> passagers;   // Collection List

    public Vol(String numeroVol, String villeDepart, String villeArrivee,
               double distanceKm, Avion avion, Pilote pilote) {
        this.numeroVol    = numeroVol;
        this.villeDepart  = villeDepart;
        this.villeArrivee = villeArrivee;
        this.distanceKm   = distanceKm;
        this.avion        = avion;
        this.pilote       = pilote;
        this.statut       = StatutVol.PROGRAMME;
        this.passagers    = new ArrayList<>();
    }

    /**
     * Ajoute un passager — lève des exceptions personnalisées
     * Concept POO : gestion d'exceptions + collections
     */
    public void ajouterPassager(Passager passager)
            throws VolCompletException, VolIndisponibleException {

        if (statut == StatutVol.ANNULE || statut == StatutVol.EN_VOL || statut == StatutVol.ARRIVE)
            throw new VolIndisponibleException("Vol " + statut + " — ajout impossible.");

        if (passagers.size() >= avion.getCapacitePassagers())
            throw new VolCompletException("Vol " + numeroVol + " complet (" + avion.getCapacitePassagers() + " places).");

        passagers.add(passager);
    }

    public boolean retirerPassager(String idPassager) {
        return passagers.removeIf(p -> p.getId().equals(idPassager));
    }

    public void changerStatut(StatutVol s) { this.statut = s; }

    // Surcharge afficherInfos (overloading)
    public void afficherInfos()              { afficherInfos(false); }
    public void afficherInfos(boolean detail) {
        System.out.println(numeroVol + " | " + villeDepart + " → " + villeArrivee
            + " | " + statut + " | " + placesDisponibles() + " places libres");
        if (detail) passagers.forEach(p -> System.out.println("  • " + p));
    }

    public int placesDisponibles() { return avion.getCapacitePassagers() - passagers.size(); }

    // Getters
    public String         getNumeroVol()    { return numeroVol; }
    public String         getVilleDepart()  { return villeDepart; }
    public String         getVilleArrivee() { return villeArrivee; }
    public double         getDistanceKm()   { return distanceKm; }
    public StatutVol      getStatut()       { return statut; }
    public Avion          getAvion()        { return avion; }
    public Pilote         getPilote()       { return pilote; }
    public List<Passager> getPassagers()    { return passagers; }

    @Override
    public String toString() {
        return numeroVol + " | " + villeDepart + " → " + villeArrivee
               + " | " + statut + " | " + placesDisponibles() + " libres";
    }
}