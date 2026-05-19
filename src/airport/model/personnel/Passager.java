package airport.model.personnel;

/** CLASSE — Passager : Héritage + surcharge constructeur */
public class Passager extends Personne {

    private String  numeroPasport;
    private String  nationalite;
    private boolean passagerPrioritaire;

    public static final int POIDS_MAX_BAGAGE_CABINE = 10;

    // Constructeur standard
    public Passager(String id, String nom, String prenom, String email,
                    String numeroPasport, String nationalite) {
        super(id, nom, prenom, email);
        this.numeroPasport       = numeroPasport;
        this.nationalite         = nationalite;
        this.passagerPrioritaire = false;
    }

    // Surcharge constructeur avec priorité
    public Passager(String id, String nom, String prenom, String email,
                    String numeroPasport, String nationalite, boolean prioritaire) {
        this(id, nom, prenom, email, numeroPasport, nationalite);
        this.passagerPrioritaire = prioritaire;
    }

    @Override
    public String getRole() {
        return passagerPrioritaire ? "Passager Prioritaire" : "Passager";
    }

    public String  getNumeroPasport()      { return numeroPasport; }
    public String  getNationalite()        { return nationalite; }
    public boolean isPassagerPrioritaire() { return passagerPrioritaire; }
    public void    setPassagerPrioritaire(boolean p) { this.passagerPrioritaire = p; }
}