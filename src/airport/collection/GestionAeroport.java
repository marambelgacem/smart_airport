package airport.collection;

import airport.exception.*;
import airport.model.avion.Avion;
import airport.model.personnel.*;
import airport.model.service.Piste;
import airport.model.vol.*;

import java.util.*;

/**
 * CLASSE — GestionAeroport
 * Concept POO : Collections (List, Set, Map) + Polymorphisme + Exceptions
 */
public class GestionAeroport {

    private final String nomAeroport;

    // MAP  : accès rapide par clé unique
    private Map<String, Vol>      vols      = new HashMap<>();
    private Map<String, Passager> passagers = new HashMap<>();

    // LIST : ordre conservé
    private List<Avion>    flotte    = new ArrayList<>();
    private List<Piste>    pistes    = new ArrayList<>();
    private List<AgentSol> agentsSol = new ArrayList<>();

    // SET  : unicité garantie (pas de doublons)
    private Set<Pilote> pilotes = new HashSet<>();

    public GestionAeroport(String nomAeroport) {
        this.nomAeroport = nomAeroport;
    }

    // ── Avions ────────────────────────────────────────────────
    public void ajouterAvion(Avion avion) { flotte.add(avion); }

    public Optional<Avion> trouverAvion(String immat) {
        return flotte.stream().filter(a -> a.getImmatriculation().equals(immat)).findFirst();
    }

    // ── Pilotes ───────────────────────────────────────────────
    public void ajouterPilote(Pilote p) { pilotes.add(p); }

    public Optional<Pilote> trouverPilote(String id) {
        return pilotes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    // ── Passagers ─────────────────────────────────────────────
    public void ajouterPassager(Passager p) { passagers.put(p.getId(), p); }
    public Passager trouverPassager(String id) { return passagers.get(id); }

    // ── Pistes ────────────────────────────────────────────────
    public void ajouterPiste(Piste p) { pistes.add(p); }

    public Optional<Piste> trouverPisteDisponible() {
        return pistes.stream().filter(Piste::isDisponible).findFirst();
    }

    // ── Vols ─────────────────────────────────────────────────
    /**
     * Crée un vol avec vérifications métier + exceptions personnalisées
     */
    public Vol creerVol(String numero, String depart, String arrivee, double distance,
                        String immatAvion, String idPilote)
            throws AvionEnMaintenanceException, PiloteNonQualifieException {

        Avion avion = trouverAvion(immatAvion)
            .orElseThrow(() -> new RuntimeException("Avion introuvable : " + immatAvion));

        if (avion.isEnMaintenance())
            throw new AvionEnMaintenanceException(immatAvion);

        Pilote pilote = trouverPilote(idPilote)
            .orElseThrow(() -> new RuntimeException("Pilote introuvable : " + idPilote));

        if (distance > 5000 && !pilote.estQualifieLongCourrier())
            throw new PiloteNonQualifieException(idPilote,
                "Heures insuffisantes pour " + distance + " km.");

        Vol vol = new Vol(numero, depart, arrivee, distance, avion, pilote);
        vols.put(numero, vol);
        return vol;
    }

    /**
     * Réserve un passager sur un vol — try/catch exceptions
     */
    public String reserverPassager(String idPassager, String numeroVol) {
        Passager passager = trouverPassager(idPassager);
        if (passager == null) return "❌ Passager introuvable : " + idPassager;

        Vol vol = vols.get(numeroVol);
        if (vol == null) return "❌ Vol introuvable : " + numeroVol;

        try {
            vol.ajouterPassager(passager);
            return "✅ " + passager.getPrenom() + " réservé sur " + numeroVol;
        } catch (airport.exception.VolCompletException e) {
            return "❌ " + e.getMessage();
        } catch (airport.exception.VolIndisponibleException e) {
            return "⚠ " + e.getMessage();
        }
    }

    /**
     * Affiche tout le personnel — démonstration du Polymorphisme
     * List<Personne> contient Pilote + AgentSol + Passager
     */
    public List<Personne> getToutLePersonnel() {
        List<Personne> all = new ArrayList<>();
        all.addAll(pilotes);
        all.addAll(agentsSol);
        all.addAll(passagers.values());
        return all;
    }

    // ── Getters ───────────────────────────────────────────────
    public String              getNomAeroport() { return nomAeroport; }
    public Map<String, Vol>    getVols()        { return vols; }
    public List<Avion>         getFlotte()      { return flotte; }
    public Set<Pilote>         getPilotes()     { return pilotes; }
    public Map<String,Passager>getPassagers()   { return passagers; }
    public List<Piste>         getPistes()      { return pistes; }
    public List<AgentSol>      getAgentsSol()   { return agentsSol; }
}