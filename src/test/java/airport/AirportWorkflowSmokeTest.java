package test.java.airport;

import airport.collection.GestionAeroport;
import airport.exception.AvionEnMaintenanceException;
import airport.exception.PiloteNonQualifieException;
import airport.model.avion.Avion;
import airport.model.personnel.AgentSol;
import airport.model.personnel.Passager;
import airport.model.personnel.Pilote;
import airport.model.service.Piste;
import airport.model.vol.StatutVol;
import airport.model.vol.Vol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportWorkflowSmokeTest {

    @Test
    void addFormsPopulateAirportCollections() {
        GestionAeroport aeroport = new GestionAeroport("Smart Airport");
        Avion avion = new Avion("TS-ADD1", "Airbus A320", 180, 6100);
        Pilote pilote = new Pilote("PIL-ADD1", "Ben Ali", "Sami", "sami@airport.tn", "LIC-ADD1", 1200, "secret");
        Passager passager = new Passager("PAS-ADD1", "Trabelsi", "Lina", "lina@mail.com", "TN10001", "Tunisienne", true);
        AgentSol agent = new AgentSol("AGS-ADD1", "Mansour", "Hedi", "hedi@airport.tn", "Bagages", 4);
        Piste piste = new Piste("09-27", 3200);

        aeroport.ajouterAvion(avion);
        aeroport.ajouterPilote(pilote);
        aeroport.ajouterPassager(passager);
        aeroport.ajouterAgentSol(agent);
        aeroport.ajouterPiste(piste);

        assertTrue(aeroport.getFlotte().contains(avion));
        assertTrue(aeroport.getPilotes().contains(pilote));
        assertEquals(passager, aeroport.trouverPassager("PAS-ADD1"));
        assertTrue(aeroport.getAgentsSol().contains(agent));
        assertTrue(aeroport.getPistes().contains(piste));
    }

    @Test
    void createFlightFormWorkflowRegistersServiceAndAllowsReservation() throws Exception {
        GestionAeroport aeroport = seededAirport();

        Vol vol = aeroport.creerVol("TU501", "Tunis", "Rome", 620, "TS-OPS1", "PIL-OPS1");
        String reservation = aeroport.reserverPassager("PAS-OPS1", "TU501");
        vol.changerStatut(StatutVol.EMBARQUEMENT);

        assertEquals("TU501", vol.getNumeroVol());
        assertNotNull(aeroport.getVols().get("TU501"));
        assertTrue(reservation.startsWith("✅"));
        assertEquals(1, vol.getPassagers().size());
        assertEquals(StatutVol.EMBARQUEMENT, vol.getStatut());
    }

    @Test
    void createFlightRejectsMaintenanceAircraft() {
        GestionAeroport aeroport = seededAirport();
        aeroport.trouverAvion("TS-OPS1").orElseThrow().mettreEnMaintenance();

        assertThrows(
            AvionEnMaintenanceException.class,
            () -> aeroport.creerVol("TU777", "Tunis", "Paris", 1800, "TS-OPS1", "PIL-OPS1")
        );
    }

    @Test
    void createFlightRejectsUnderqualifiedPilotOnLongHaul() {
        GestionAeroport aeroport = seededAirport();
        aeroport.ajouterPilote(new Pilote("PIL-JR1", "Junior", "Pilot", "junior@airport.tn", "LIC-JR1", 400, "secret"));

        assertThrows(
            PiloteNonQualifieException.class,
            () -> aeroport.creerVol("TU990", "Tunis", "Montreal", 6000, "TS-LH1", "PIL-JR1")
        );
    }

    @Test
    void maintenanceAndRunwayFormsApplyOperationalChanges() throws Exception {
        GestionAeroport aeroport = seededAirport();
        Vol vol = aeroport.creerVol("TU880", "Tunis", "Marseille", 900, "TS-OPS1", "PIL-OPS1");
        Piste piste = aeroport.getPistes().get(0);
        Avion avion = aeroport.trouverAvion("TS-OPS1").orElseThrow();

        avion.mettreEnMaintenance();
        assertTrue(avion.isEnMaintenance());

        avion.sortirDeMaintenance();
        assertFalse(avion.isEnMaintenance());

        assertTrue(piste.assignerVol(vol));
        assertEquals(vol, piste.getVolEnCours());
        assertFalse(piste.isDisponible());

        piste.libererPiste();
        assertTrue(piste.isDisponible());
        assertNull(piste.getVolEnCours());
    }

    private GestionAeroport seededAirport() {
        GestionAeroport aeroport = new GestionAeroport("Smart Airport");
        aeroport.ajouterAvion(new Avion("TS-OPS1", "Airbus A320", 180, 6100));
        aeroport.ajouterAvion(new Avion("TS-LH1", "Airbus A330", 260, 12500));
        aeroport.ajouterPilote(new Pilote("PIL-OPS1", "Kefi", "Nour", "nour@airport.tn", "LIC-OPS1", 1500, "secret"));
        aeroport.ajouterPassager(new Passager("PAS-OPS1", "Ben Salem", "Meriem", "meriem@mail.com", "TN20002", "Tunisienne"));
        aeroport.ajouterAgentSol(new AgentSol("AGS-OPS1", "Youssef", "Omar", "omar@airport.tn", "Enregistrement", 5));
        aeroport.ajouterPiste(new Piste("01-19", 3600));
        return aeroport;
    }
}
