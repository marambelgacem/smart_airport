package airport.model.service;

import airport.model.vol.Vol;

/** CLASSE — Piste : association avec Vol */
public class Piste {

    private String  numeroPiste;
    private double  longueurMetres;
    private boolean disponible;
    private Vol     volEnCours;

    public static final double LONGUEUR_MIN_GROS_PORTEUR = 3000.0;

    public Piste(String numeroPiste, double longueurMetres) {
        this.numeroPiste    = numeroPiste;
        this.longueurMetres = longueurMetres;
        this.disponible     = true;
        this.volEnCours     = null;
    }

    public boolean assignerVol(Vol vol) {
        if (!disponible) return false;
        this.volEnCours = vol;
        this.disponible = false;
        return true;
    }

    public void libererPiste() {
        this.volEnCours = null;
        this.disponible = true;
    }

    public String  getNumeroPiste()    { return numeroPiste; }
    public double  getLongueurMetres() { return longueurMetres; }
    public boolean isDisponible()      { return disponible; }
    public Vol     getVolEnCours()     { return volEnCours; }

    @Override
    public String toString() {
        return "Piste " + numeroPiste + " (" + longueurMetres + "m) - "
               + (disponible ? "LIBRE" : "OCCUPÉE");
    }
}