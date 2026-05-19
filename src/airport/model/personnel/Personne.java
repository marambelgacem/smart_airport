package airport.model.personnel;

import java.util.Objects;

/**
 * CLASSE ABSTRAITE — Personne
 * Concept POO : Abstraction + Encapsulation + base de l'Héritage
 */
public abstract class Personne {

    private String id;
    private String nom;
    private String prenom;
    private String email;

    public static final String FORMAT_ID = "PERS-XXXX";

    public Personne(String id, String nom, String prenom, String email) {
        this.id     = id;
        this.nom    = nom;
        this.prenom = prenom;
        this.email  = email;
    }

    // Méthode abstraite : chaque sous-classe DOIT la définir
    public abstract String getRole();

    public void afficherInfos() {
        System.out.println("ID: " + id + " | " + nom + " " + prenom + " | " + getRole());
    }

    public String getId()     { return id; }
    public String getNom()    { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail()  { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Personne personne)) return false;
        return Objects.equals(id, personne.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[" + getRole() + "] " + prenom + " " + nom + " (" + id + ")";
    }
}
