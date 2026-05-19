package airport.model.personnel;

/** INTERFACE — Authentifiable : contrat de connexion */
public interface Authentifiable {
    int MAX_TENTATIVES = 3;
    boolean seConnecter(String motDePasse);
    void seDeconnecter();
    boolean estConnecte();
}