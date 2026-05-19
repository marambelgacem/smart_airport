package airport.model.vol;

/** ENUM — StatutVol : tous les états possibles d'un vol */
public enum StatutVol {
    PROGRAMME   ("Programmé",    "📅"),
    EMBARQUEMENT("Embarquement", "🚪"),
    EN_VOL      ("En Vol",       "✈"),
    ARRIVE      ("Arrivé",       "🛬"),
    ANNULE      ("Annulé",       "❌"),
    RETARDE     ("Retardé",      "⏰");

    private final String libelle;
    private final String emoji;

    StatutVol(String libelle, String emoji) {
        this.libelle = libelle;
        this.emoji   = emoji;
    }

    public String getLibelle() { return libelle; }
    public String getEmoji()   { return emoji; }

    @Override
    public String toString() { return emoji + " " + libelle; }
}