package airport.ui;

import airport.collection.GestionAeroport;
import airport.exception.AvionEnMaintenanceException;
import airport.exception.PiloteNonQualifieException;
import airport.model.avion.Avion;
import airport.model.personnel.*;
import airport.model.service.Piste;
import airport.model.vol.*;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ================================================================
 *  CLASSE PRINCIPALE : AirportApp
 *  Interface Graphique JavaFX — SmartAirport
 *  Thème : Dark mode professionnel
 * ================================================================
 */
public class AirportApp extends Application {

    // ── Système de gestion ────────────────────────────────────
    private GestionAeroport aeroport;

    // ── Couleurs (thème sombre) ───────────────────────────────
    private static final String C_BG        = "#0D1117";
    private static final String C_SURFACE   = "#161B22";
    private static final String C_CARD      = "#1C2333";
    private static final String C_BORDER    = "#30363D";
    private static final String C_BLUE      = "#58A6FF";
    private static final String C_CYAN      = "#39D0D8";
    private static final String C_GREEN     = "#3FB950";
    private static final String C_YELLOW    = "#D29922";
    private static final String C_RED       = "#F85149";
    private static final String C_TEXT      = "#E6EDF3";
    private static final String C_TEXT2     = "#8B949E";
    private static final String C_TEXT3     = "#484F58";

    // ── Layout ────────────────────────────────────────────────
    private BorderPane   root;
    private StackPane    mainContent;
    private VBox         sidebar;
    private Label        clockLbl;
    private Button       activeBtn;

    // ═══════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        initDemoData();

        root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");

        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());

        mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color:" + C_BG + ";");
        root.setCenter(mainContent);

        showDashboard();

        Scene scene = new Scene(root, 1300, 800);
        stage.setTitle("SmartAirport — Système de Gestion");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.show();

        animateSidebarIn();
    }

    // ═══════════════════════════════════════════════════════════
    //  TOP BAR
    // ═══════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(12);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(56);
        bar.setStyle("-fx-background-color:" + C_SURFACE + ";"
                   + "-fx-border-color:" + C_BORDER + ";"
                   + "-fx-border-width:0 0 1 0;");

        // Icône avion animée
        Label ico = new Label("✈");
        ico.setStyle("-fx-font-size:20px; -fx-text-fill:" + C_CYAN + ";");
        RotateTransition rt = new RotateTransition(Duration.seconds(8), ico);
        rt.setByAngle(360); rt.setCycleCount(Animation.INDEFINITE); rt.play();

        // Titre
        Label title = new Label("SMART AIRPORT");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold;"
                     + "-fx-text-fill:" + C_TEXT + "; -fx-font-family:'Courier New';");
        Label sub = new Label("— " + aeroport.getNomAeroport());
        sub.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_TEXT2 + ";");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Horloge
        clockLbl = new Label();
        clockLbl.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_TEXT2 + ";"
                        + "-fx-font-family:'Courier New';");
        Timeline clk = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLbl.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy")));
        }));
        clk.setCycleCount(Animation.INDEFINITE); clk.play();

        // Badge statut
        Circle dot = new Circle(5, Color.web(C_GREEN));
        Label statut = new Label("  Système opérationnel");
        statut.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_GREEN + ";");
        HBox badge = new HBox(dot, statut);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color:rgba(63,185,80,0.1);"
                     + "-fx-background-radius:20; -fx-padding:5 14 5 10;");

        bar.getChildren().addAll(ico, title, sub, sp, clockLbl, badge);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        sidebar = new VBox(2);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color:" + C_SURFACE + ";"
                       + "-fx-border-color:" + C_BORDER + ";"
                       + "-fx-border-width:0 1 0 0;");

        Label nav = mkLabel("NAVIGATION", 9, C_TEXT3);
        nav.setPadding(new Insets(0, 0, 10, 8));

        Button b0 = mkNavBtn("📊", "Tableau de bord");
        Button b1 = mkNavBtn("✈",  "Vols");
        Button b2 = mkNavBtn("🛩", "Flotte d'avions");
        Button b3 = mkNavBtn("👨‍✈️","Pilotes");
        Button b4 = mkNavBtn("🧑", "Passagers");
        Button b5 = mkNavBtn("🛣", "Pistes");
        Button b6 = mkNavBtn("👥", "Personnel");

        b0.setOnAction(e -> { setActive(b0); showDashboard(); });
        b1.setOnAction(e -> { setActive(b1); showVols(); });
        b2.setOnAction(e -> { setActive(b2); showAvions(); });
        b3.setOnAction(e -> { setActive(b3); showPilotes(); });
        b4.setOnAction(e -> { setActive(b4); showPassagers(); });
        b5.setOnAction(e -> { setActive(b5); showPistes(); });
        b6.setOnAction(e -> { setActive(b6); showPersonnel(); });

        Region sp = new Region(); VBox.setVgrow(sp, Priority.ALWAYS);

        Button quit = new Button("🚪  Quitter");
        quit.setMaxWidth(Double.MAX_VALUE);
        styleQuitBtn(quit, false);
        quit.setOnMouseEntered(e -> styleQuitBtn(quit, true));
        quit.setOnMouseExited(e  -> styleQuitBtn(quit, false));
        quit.setOnAction(e -> Platform.exit());

        sidebar.getChildren().addAll(nav, b0, b1, b2, b3, b4, b5, b6, sp, quit);
        setActive(b0);
        return sidebar;
    }

    private Button mkNavBtn(String icon, String label) {
        Button b = new Button(icon + "  " + label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        styleNavBtn(b, false);
        b.setOnMouseEntered(e -> { if (b != activeBtn) styleNavBtn(b, true); });
        b.setOnMouseExited(e  -> { if (b != activeBtn) styleNavBtn(b, false); });
        return b;
    }

    private void styleNavBtn(Button b, boolean hover) {
        b.setStyle("-fx-background-color:" + (hover ? "rgba(88,166,255,0.1)" : "transparent") + ";"
                 + "-fx-text-fill:" + (hover ? C_TEXT : C_TEXT2) + ";"
                 + "-fx-font-size:12px; -fx-cursor:hand; -fx-background-radius:8;"
                 + "-fx-padding:10 12 10 12;");
    }

    private void styleQuitBtn(Button b, boolean hover) {
        b.setStyle("-fx-background-color:" + (hover ? "rgba(248,81,73,0.25)" : "rgba(248,81,73,0.1)") + ";"
                 + "-fx-text-fill:" + C_RED + "; -fx-font-size:12px; -fx-cursor:hand;"
                 + "-fx-background-radius:8; -fx-padding:10 12 10 12;");
    }

    private void setActive(Button b) {
        if (activeBtn != null) styleNavBtn(activeBtn, false);
        activeBtn = b;
        b.setStyle("-fx-background-color:rgba(88,166,255,0.2);"
                 + "-fx-text-fill:" + C_BLUE + ";"
                 + "-fx-font-size:12px; -fx-cursor:hand; -fx-background-radius:8;"
                 + "-fx-padding:10 12 10 15;"
                 + "-fx-border-color:" + C_BLUE + "; -fx-border-width:0 0 0 3;"
                 + "-fx-border-radius:0 8 8 0;");
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════
    private void showDashboard() {
        VBox page = page();

        page.getChildren().add(pageTitle("📊", "Tableau de Bord", "Vue d'ensemble en temps réel"));

        // ── KPI Cards ─────────────────────────────────────────
        HBox kpis = new HBox(14);
        kpis.getChildren().addAll(
            kpiCard("✈",  "Avions",    String.valueOf(aeroport.getFlotte().size()),     C_CYAN,   "dans la flotte"),
            kpiCard("👨‍✈️","Pilotes",   String.valueOf(aeroport.getPilotes().size()),    C_BLUE,   "enregistrés"),
            kpiCard("🧑", "Passagers", String.valueOf(aeroport.getPassagers().size()),   C_GREEN,  "inscrits"),
            kpiCard("📋", "Vols",      String.valueOf(aeroport.getVols().size()),        C_YELLOW, "programmés"),
            kpiCard("🛣", "Pistes",    String.valueOf(aeroport.getPistes().size()),      C_RED,    "au total")
        );
        page.getChildren().add(kpis);

        // ── Tableau des vols ──────────────────────────────────
        page.getChildren().add(sectionLabel("Vols en cours"));
        TableView<Vol> tv = buildVolsTable();
        tv.setMaxHeight(230);
        page.getChildren().add(tv);

        // ── Ligne du bas ──────────────────────────────────────
        HBox row = new HBox(14);
        row.getChildren().addAll(infoCard(), pistesCard());
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        page.getChildren().add(row);

        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  VOLS
    // ═══════════════════════════════════════════════════════════
    private void showVols() {
        VBox page = page();
        page.getChildren().add(pageTitle("✈", "Gestion des Vols", "Créer, réserver, changer le statut"));

        // ── Formulaire création ───────────────────────────────
        VBox formCard = card();
        formCard.getChildren().add(sectionLabel("Créer un nouveau vol"));

        GridPane g = grid();
        TextField tfNum   = field("ex: TU105");
        TextField tfDep   = field("ex: Tunis");
        TextField tfArr   = field("ex: Paris");
        TextField tfDist  = field("ex: 1700");
        TextField tfImmat = field("ex: TU-JAL");
        TextField tfPilot = field("ex: PIL001");

        g.add(lbl("N° Vol"),          0,0); g.add(tfNum,   1,0);
        g.add(lbl("Départ"),          2,0); g.add(tfDep,   3,0);
        g.add(lbl("Arrivée"),         0,1); g.add(tfArr,   1,1);
        g.add(lbl("Distance (km)"),   2,1); g.add(tfDist,  3,1);
        g.add(lbl("Immatriculation"), 0,2); g.add(tfImmat, 1,2);
        g.add(lbl("ID Pilote"),       2,2); g.add(tfPilot, 3,2);

        Button btnAdd = btn("➕  Créer le vol", C_BLUE);
        Label  msg    = msgLabel();

        btnAdd.setOnAction(e -> {
            try {
                double dist = Double.parseDouble(tfDist.getText().trim());
                aeroport.creerVol(tfNum.getText().trim(), tfDep.getText().trim(),
                        tfArr.getText().trim(), dist,
                        tfImmat.getText().trim(), tfPilot.getText().trim());
                msg.setText("✅ Vol " + tfNum.getText() + " créé !");
                msg.setStyle(msgStyle(C_GREEN));
                showVols();
            } catch (AvionEnMaintenanceException ex) {
                msg.setText("🔧 " + ex.getMessage());
                msg.setStyle(msgStyle(C_YELLOW));
            } catch (PiloteNonQualifieException ex) {
                msg.setText("⚠ " + ex.getMessage());
                msg.setStyle(msgStyle(C_RED));
            } catch (Exception ex) {
                msg.setText("❌ " + ex.getMessage());
                msg.setStyle(msgStyle(C_RED));
            }
        });

        g.add(new HBox(10, btnAdd, msg), 0, 3, 4, 1);
        formCard.getChildren().add(g);

        // ── Formulaire réservation ────────────────────────────
        VBox resCard = card();
        resCard.getChildren().add(sectionLabel("Réserver un passager"));
        HBox resRow  = new HBox(10);
        resRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfPas = field("ID Passager"); tfPas.setMaxWidth(140);
        TextField tfVol = field("N° Vol");      tfVol.setMaxWidth(130);
        Button btnRes   = btn("🎫  Réserver", C_GREEN);
        Label  msgRes   = msgLabel();

        btnRes.setOnAction(e -> {
            String r = aeroport.reserverPassager(tfPas.getText().trim(), tfVol.getText().trim());
            msgRes.setText(r);
            msgRes.setStyle(msgStyle(r.startsWith("✅") ? C_GREEN : C_RED));
            showVols();
        });
        resRow.getChildren().addAll(
            lbl("Passager :"), tfPas, lbl("Vol :"), tfVol, btnRes, msgRes);
        resCard.getChildren().add(resRow);

        // ── Table vols ────────────────────────────────────────
        page.getChildren().addAll(formCard, resCard);
        page.getChildren().add(sectionLabel("Liste de tous les vols"));

        TableView<Vol> table = buildVolsTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().add(table);

        // ── Changer statut ────────────────────────────────────
        VBox statCard = card();
        statCard.getChildren().add(sectionLabel("Changer le statut d'un vol"));
        HBox statRow = new HBox(10); statRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfStatVol = field("N° Vol"); tfStatVol.setMaxWidth(130);
        ComboBox<StatutVol> cbStat = new ComboBox<>(
            FXCollections.observableArrayList(StatutVol.values()));
        cbStat.setStyle(comboStyle()); cbStat.getSelectionModel().selectFirst();
        Button btnStat = btn("🔄  Changer", C_YELLOW);
        Label  msgStat = msgLabel();

        btnStat.setOnAction(e -> {
            Vol v = aeroport.getVols().get(tfStatVol.getText().trim());
            if (v == null) {
                msgStat.setText("❌ Vol introuvable"); msgStat.setStyle(msgStyle(C_RED));
            } else {
                v.changerStatut(cbStat.getValue());
                msgStat.setText("✅ Statut mis à jour"); msgStat.setStyle(msgStyle(C_GREEN));
                showVols();
            }
        });
        statRow.getChildren().addAll(lbl("Vol :"), tfStatVol, cbStat, btnStat, msgStat);
        statCard.getChildren().add(statRow);
        page.getChildren().add(statCard);

        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  AVIONS
    // ═══════════════════════════════════════════════════════════
    private void showAvions() {
        VBox page = page();
        page.getChildren().add(pageTitle("🛩", "Flotte d'Avions", "Gérer les appareils et la maintenance"));

        // ── Table flotte ──────────────────────────────────────
        TableView<Avion> table = new TableView<>();
        table.setStyle(tableStyle()); table.setMaxHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Avion,String> c1 = tc("Immatriculation",140);
        TableColumn<Avion,String> c2 = tc("Modèle",170);
        TableColumn<Avion,String> c3 = tc("Capacité",110);
        TableColumn<Avion,String> c4 = tc("Autonomie",120);
        TableColumn<Avion,String> c5 = tc("Statut",130);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getImmatriculation()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getModele()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCapacitePassagers() + " pax"));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAutonomieKm() + " km"));
        c5.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().isEnMaintenance() ? "🔧 Maintenance" : "✅ Opérationnel"));
        colorCell(c5, "Maintenance", C_YELLOW, C_GREEN);

        table.getColumns().addAll(c1,c2,c3,c4,c5);
        table.setItems(FXCollections.observableArrayList(aeroport.getFlotte()));

        // ── Formulaire ajout avion ────────────────────────────
        VBox addCard = card();
        addCard.getChildren().add(sectionLabel("Ajouter un avion à la flotte"));
        GridPane g = grid();
        TextField tfIm = field("ex: TU-NEO");
        TextField tfMo = field("ex: Airbus A321");
        TextField tfCa = field("ex: 220");
        TextField tfAu = field("ex: 7000");
        g.add(lbl("Immatriculation"),0,0); g.add(tfIm,1,0);
        g.add(lbl("Modèle"),        2,0); g.add(tfMo,3,0);
        g.add(lbl("Capacité (pax)"),0,1); g.add(tfCa,1,1);
        g.add(lbl("Autonomie (km)"),2,1); g.add(tfAu,3,1);
        Button btnA = btn("➕  Ajouter", C_CYAN); Label msgA = msgLabel();
        btnA.setOnAction(e -> {
            try {
                aeroport.ajouterAvion(new Avion(tfIm.getText().trim(), tfMo.getText().trim(),
                    Integer.parseInt(tfCa.getText().trim()),
                    Double.parseDouble(tfAu.getText().trim())));
                msgA.setText("✅ Avion ajouté !"); msgA.setStyle(msgStyle(C_GREEN));
                showAvions();
            } catch (Exception ex) { msgA.setText("❌ "+ex.getMessage()); msgA.setStyle(msgStyle(C_RED)); }
        });
        g.add(new HBox(10, btnA, msgA), 0, 2, 4, 1);
        addCard.getChildren().add(g);

        // ── Maintenance ───────────────────────────────────────
        VBox maintCard = card();
        maintCard.getChildren().add(sectionLabel("Gestion de la maintenance"));
        HBox maintRow = new HBox(10); maintRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfMaint = field("Immatriculation"); tfMaint.setMaxWidth(160);
        Button btnMaintOn  = btn("🔧 Mettre en maintenance", C_YELLOW);
        Button btnMaintOff = btn("✅ Sortir de maintenance",  C_GREEN);
        Label  msgMaint    = msgLabel();

        btnMaintOn.setOnAction(e -> aeroport.trouverAvion(tfMaint.getText().trim()).ifPresentOrElse(a -> {
            a.mettreEnMaintenance();
            msgMaint.setText("🔧 " + a.getImmatriculation() + " en maintenance");
            msgMaint.setStyle(msgStyle(C_YELLOW));
            table.setItems(FXCollections.observableArrayList(aeroport.getFlotte()));
        }, () -> { msgMaint.setText("❌ Introuvable"); msgMaint.setStyle(msgStyle(C_RED)); }));

        btnMaintOff.setOnAction(e -> aeroport.trouverAvion(tfMaint.getText().trim()).ifPresentOrElse(a -> {
            a.sortirDeMaintenance();
            msgMaint.setText("✅ " + a.getImmatriculation() + " opérationnel");
            msgMaint.setStyle(msgStyle(C_GREEN));
            table.setItems(FXCollections.observableArrayList(aeroport.getFlotte()));
        }, () -> { msgMaint.setText("❌ Introuvable"); msgMaint.setStyle(msgStyle(C_RED)); }));

        maintRow.getChildren().addAll(lbl("Avion :"), tfMaint, btnMaintOn, btnMaintOff, msgMaint);
        maintCard.getChildren().add(maintRow);

        page.getChildren().add(sectionLabel("Flotte actuelle"));
        page.getChildren().addAll(table, addCard, maintCard);
        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  PILOTES
    // ═══════════════════════════════════════════════════════════
    private void showPilotes() {
        VBox page = page();
        page.getChildren().add(pageTitle("👨‍✈️", "Gestion des Pilotes", "Personnel navigant"));

        // ── Table pilotes ─────────────────────────────────────
        TableView<Pilote> table = new TableView<>();
        table.setStyle(tableStyle()); table.setMaxHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Pilote,String> c1 = tc("ID",80);
        TableColumn<Pilote,String> c2 = tc("Nom complet",170);
        TableColumn<Pilote,String> c3 = tc("Licence",110);
        TableColumn<Pilote,String> c4 = tc("Heures de vol",130);
        TableColumn<Pilote,String> c5 = tc("Email",200);
        TableColumn<Pilote,String> c6 = tc("Long-courrier",120);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()+" "+d.getValue().getNom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLicencePilote()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeuresDeVol()+" h"));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        c6.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().estQualifieLongCourrier() ? "✅ Qualifié" : "❌ Non qualifié"));
        colorCell(c6, "✅", C_GREEN, C_YELLOW);

        table.getColumns().addAll(c1,c2,c3,c4,c5,c6);
        table.setItems(FXCollections.observableArrayList(aeroport.getPilotes()));

        // ── Formulaire ajout ──────────────────────────────────
        VBox addCard = card();
        addCard.getChildren().add(sectionLabel("Enregistrer un nouveau pilote"));
        GridPane g = grid();
        TextField tfId  = field("ex: PIL004"); TextField tfNom = field("Nom");
        TextField tfPrn = field("Prénom");     TextField tfEm  = field("email@airport.tn");
        TextField tfLic = field("ex: LIC-004");TextField tfHV  = field("ex: 500");
        PasswordField tfMdp = new PasswordField(); tfMdp.setStyle(fieldStyle());
        tfMdp.setPromptText("Mot de passe"); tfMdp.setPrefWidth(160);

        g.add(lbl("ID"),           0,0); g.add(tfId, 1,0);
        g.add(lbl("Nom"),          2,0); g.add(tfNom,3,0);
        g.add(lbl("Prénom"),       0,1); g.add(tfPrn,1,1);
        g.add(lbl("Email"),        2,1); g.add(tfEm, 3,1);
        g.add(lbl("Licence"),      0,2); g.add(tfLic,1,2);
        g.add(lbl("Heures de vol"),2,2); g.add(tfHV, 3,2);
        g.add(lbl("Mot de passe"), 0,3); g.add(tfMdp,1,3);

        Button btnA = btn("➕  Enregistrer", C_BLUE); Label msgA = msgLabel();
        btnA.setOnAction(e -> {
            try {
                aeroport.ajouterPilote(new Pilote(
                    tfId.getText().trim(), tfNom.getText().trim(),
                    tfPrn.getText().trim(), tfEm.getText().trim(),
                    tfLic.getText().trim(), Integer.parseInt(tfHV.getText().trim()),
                    tfMdp.getText()));
                msgA.setText("✅ Pilote enregistré !"); msgA.setStyle(msgStyle(C_GREEN));
                showPilotes();
            } catch (Exception ex) { msgA.setText("❌ "+ex.getMessage()); msgA.setStyle(msgStyle(C_RED)); }
        });
        g.add(new HBox(10,btnA,msgA),0,4,4,1);
        addCard.getChildren().add(g);

        page.getChildren().add(sectionLabel("Liste des pilotes"));
        page.getChildren().addAll(table, addCard);
        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  PASSAGERS
    // ═══════════════════════════════════════════════════════════
    private void showPassagers() {
        VBox page = page();
        page.getChildren().add(pageTitle("🧑", "Gestion des Passagers", "Voyageurs enregistrés"));

        // ── Table passagers ───────────────────────────────────
        TableView<Passager> table = new TableView<>();
        table.setStyle(tableStyle()); table.setMaxHeight(250);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Passager,String> c1 = tc("ID",80);
        TableColumn<Passager,String> c2 = tc("Nom complet",170);
        TableColumn<Passager,String> c3 = tc("Passeport",120);
        TableColumn<Passager,String> c4 = tc("Nationalité",130);
        TableColumn<Passager,String> c5 = tc("Email",200);
        TableColumn<Passager,String> c6 = tc("Prioritaire",100);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()+" "+d.getValue().getNom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroPasport()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNationalite()));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        c6.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().isPassagerPrioritaire() ? "⭐ OUI" : "NON"));
        colorCell(c6, "⭐", C_YELLOW, C_TEXT2);

        table.getColumns().addAll(c1,c2,c3,c4,c5,c6);
        table.setItems(FXCollections.observableArrayList(aeroport.getPassagers().values()));

        // ── Formulaire ajout passager ─────────────────────────
        VBox addCard = card();
        addCard.getChildren().add(sectionLabel("Inscrire un nouveau passager"));
        GridPane g = grid();
        TextField tfId  = field("ex: PAS006"); TextField tfNom = field("Nom");
        TextField tfPrn = field("Prénom");     TextField tfEm  = field("email@gmail.com");
        TextField tfPas = field("ex: TN999999");TextField tfNat = field("ex: Tunisien");
        CheckBox  cbPri = new CheckBox("Passager prioritaire");
        cbPri.setStyle("-fx-text-fill:"+C_TEXT2+"; -fx-font-size:12px;");

        g.add(lbl("ID"),         0,0); g.add(tfId, 1,0);
        g.add(lbl("Nom"),        2,0); g.add(tfNom,3,0);
        g.add(lbl("Prénom"),     0,1); g.add(tfPrn,1,1);
        g.add(lbl("Email"),      2,1); g.add(tfEm, 3,1);
        g.add(lbl("Passeport"),  0,2); g.add(tfPas,1,2);
        g.add(lbl("Nationalité"),2,2); g.add(tfNat,3,2);
        g.add(cbPri,             0,3,2,1);

        Button btnA = btn("➕  Inscrire", C_GREEN); Label msgA = msgLabel();
        btnA.setOnAction(e -> {
            try {
                aeroport.ajouterPassager(new Passager(
                    tfId.getText().trim(), tfNom.getText().trim(),
                    tfPrn.getText().trim(), tfEm.getText().trim(),
                    tfPas.getText().trim(), tfNat.getText().trim(),
                    cbPri.isSelected()));
                msgA.setText("✅ Passager inscrit !"); msgA.setStyle(msgStyle(C_GREEN));
                showPassagers();
            } catch (Exception ex) { msgA.setText("❌ "+ex.getMessage()); msgA.setStyle(msgStyle(C_RED)); }
        });
        g.add(new HBox(10,btnA,msgA),0,4,4,1);
        addCard.getChildren().add(g);

        page.getChildren().add(sectionLabel("Liste des passagers"));
        page.getChildren().addAll(table, addCard);
        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  PISTES
    // ═══════════════════════════════════════════════════════════
    private void showPistes() {
        VBox page = page();
        page.getChildren().add(pageTitle("🛣", "Gestion des Pistes", "Pistes d'atterrissage et de décollage"));

        // ── Cartes visuelles pistes ───────────────────────────
        FlowPane fp = new FlowPane(14, 14);
        for (Piste p : aeroport.getPistes()) {
            fp.getChildren().add(buildPisteCard(p));
        }
        page.getChildren().add(fp);

        // ── Ajouter une piste ─────────────────────────────────
        VBox addCard = card();
        addCard.getChildren().add(sectionLabel("Ajouter une piste"));
        HBox addRow = new HBox(10); addRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfNp = field("N° piste"); tfNp.setMaxWidth(120);
        TextField tfLg = field("Longueur (m)"); tfLg.setMaxWidth(140);
        Button btnAdd = btn("➕  Ajouter", C_CYAN); Label msgAdd = msgLabel();
        btnAdd.setOnAction(e -> {
            try {
                aeroport.ajouterPiste(new Piste(tfNp.getText().trim(),
                    Double.parseDouble(tfLg.getText().trim())));
                msgAdd.setText("✅ Piste ajoutée !"); msgAdd.setStyle(msgStyle(C_GREEN));
                showPistes();
            } catch (Exception ex) { msgAdd.setText("❌ "+ex.getMessage()); msgAdd.setStyle(msgStyle(C_RED)); }
        });
        addRow.getChildren().addAll(lbl("N° :"), tfNp, lbl("Longueur :"), tfLg, btnAdd, msgAdd);
        addCard.getChildren().add(addRow);

        // ── Actions pistes ────────────────────────────────────
        VBox actCard = card();
        actCard.getChildren().add(sectionLabel("Assigner / Libérer une piste"));
        GridPane g = grid();
        TextField tfPiste = field("N° piste"); TextField tfVol = field("N° vol");
        Button btnAss = btn("📍 Assigner vol", C_BLUE);
        Button btnLib = btn("🔓 Libérer",      C_CYAN);
        Label  msgAct = msgLabel();

        btnAss.setOnAction(e -> {
            Vol v = aeroport.getVols().get(tfVol.getText().trim());
            if (v == null) { msgAct.setText("❌ Vol introuvable"); msgAct.setStyle(msgStyle(C_RED)); return; }
            aeroport.getPistes().stream()
                .filter(p -> p.getNumeroPiste().equals(tfPiste.getText().trim()))
                .findFirst().ifPresentOrElse(p -> {
                    boolean ok = p.assignerVol(v);
                    msgAct.setText(ok ? "✅ Assigné" : "❌ Piste occupée");
                    msgAct.setStyle(msgStyle(ok ? C_GREEN : C_RED));
                    showPistes();
                }, () -> { msgAct.setText("❌ Piste introuvable"); msgAct.setStyle(msgStyle(C_RED)); });
        });
        btnLib.setOnAction(e -> {
            aeroport.getPistes().stream()
                .filter(p -> p.getNumeroPiste().equals(tfPiste.getText().trim()))
                .findFirst().ifPresentOrElse(p -> {
                    p.libererPiste();
                    msgAct.setText("🔓 Piste libérée"); msgAct.setStyle(msgStyle(C_CYAN));
                    showPistes();
                }, () -> { msgAct.setText("❌ Introuvable"); msgAct.setStyle(msgStyle(C_RED)); });
        });
        g.add(lbl("Piste :"),0,0); g.add(tfPiste,1,0);
        g.add(lbl("Vol :"),  2,0); g.add(tfVol,  3,0);
        g.add(new HBox(10, btnAss, btnLib, msgAct), 0, 1, 4, 1);
        actCard.getChildren().add(g);

        page.getChildren().addAll(addCard, actCard);
        swap(scrollOf(page));
    }

    private VBox buildPisteCard(Piste p) {
        boolean libre = p.isDisponible();
        String  col   = libre ? C_GREEN : C_RED;
        VBox card = new VBox(8);
        card.setPrefWidth(210);
        card.setPadding(new Insets(18, 16, 18, 16));
        card.setStyle("-fx-background-color:" + C_CARD + ";"
                    + "-fx-background-radius:12;"
                    + "-fx-border-color:" + col + ";"
                    + "-fx-border-width:1; -fx-border-radius:12;");

        Label num  = mkLabel("Piste " + p.getNumeroPiste(), 15, C_TEXT);
        num.setStyle(num.getStyle() + "-fx-font-weight:bold;");
        Label long_ = mkLabel(p.getLongueurMetres() + " m", 11, C_TEXT2);
        Label stat  = mkLabel(libre ? "✅  LIBRE" : "🔴  OCCUPÉE", 13, col);

        card.getChildren().addAll(num, long_, stat);
        if (!libre && p.getVolEnCours() != null)
            card.getChildren().add(mkLabel("Vol : " + p.getVolEnCours().getNumeroVol(), 10, C_TEXT3));
        return card;
    }

    // ═══════════════════════════════════════════════════════════
    //  PERSONNEL (Polymorphisme)
    // ═══════════════════════════════════════════════════════════
    private void showPersonnel() {
        VBox page = page();
        page.getChildren().add(pageTitle("👥", "Tout le Personnel", "Vue polymorphique"));

        // Note pédagogique
        Label note = new Label(
            "💡  POLYMORPHISME : cette vue utilise une seule List<Personne> qui contient " +
            "des Pilote, AgentSol et Passager. Java appelle automatiquement le bon getRole() " +
            "pour chaque objet — c'est le polymorphisme en action !");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill:" + C_CYAN + "; -fx-font-size:11px;"
                    + "-fx-background-color:rgba(57,208,216,0.08);"
                    + "-fx-background-radius:8; -fx-padding:12 16 12 16;"
                    + "-fx-border-color:rgba(57,208,216,0.3);"
                    + "-fx-border-radius:8; -fx-border-width:1;");
        page.getChildren().add(note);

        // Table polymorphique — List<Personne>
        TableView<Personne> table = new TableView<>();
        table.setStyle(tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Personne,String> c1 = tc("ID",85);
        TableColumn<Personne,String> c2 = tc("Prénom",120);
        TableColumn<Personne,String> c3 = tc("Nom",120);
        TableColumn<Personne,String> c4 = tc("Rôle (getRole())",210);
        TableColumn<Personne,String> c5 = tc("Email",220);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        // Coloriage par type
        c4.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                if      (v.startsWith("Pilote"))   setStyle("-fx-text-fill:"+C_CYAN+";  -fx-font-weight:bold;");
                else if (v.startsWith("Agent"))    setStyle("-fx-text-fill:"+C_YELLOW+";");
                else if (v.startsWith("Passager")) setStyle("-fx-text-fill:"+C_GREEN+";");
                else                               setStyle("-fx-text-fill:"+C_TEXT2+";");
            }
        });

        table.getColumns().addAll(c1,c2,c3,c4,c5);
        // POLYMORPHISME : List<Personne> unifiée
        table.setItems(FXCollections.observableArrayList(aeroport.getToutLePersonnel()));
        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().add(sectionLabel("Liste unifiée du personnel (List<Personne>)"));
        page.getChildren().add(table);
        swap(scrollOf(page));
    }

    // ═══════════════════════════════════════════════════════════
    //  TABLE DES VOLS (réutilisable)
    // ═══════════════════════════════════════════════════════════
    private TableView<Vol> buildVolsTable() {
        TableView<Vol> table = new TableView<>();
        table.setStyle(tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Vol,String> c1 = tc("N° Vol",  80);
        TableColumn<Vol,String> c2 = tc("Trajet",  190);
        TableColumn<Vol,String> c3 = tc("Distance",100);
        TableColumn<Vol,String> c4 = tc("Statut",  150);
        TableColumn<Vol,String> c5 = tc("Pilote",  160);
        TableColumn<Vol,String> c6 = tc("Places",   80);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroVol()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVilleDepart()+" → "+d.getValue().getVilleArrivee()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDistanceKm()+" km"));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut().toString()));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPilote().getPrenom()+" "+d.getValue().getPilote().getNom()));
        c6.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().placesDisponibles()+" libres"));

        c4.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                if      (v.contains("Vol"))    setStyle("-fx-text-fill:"+C_CYAN+"; -fx-font-weight:bold;");
                else if (v.contains("Annulé")) setStyle("-fx-text-fill:"+C_RED+";");
                else if (v.contains("Arrivé")) setStyle("-fx-text-fill:"+C_GREEN+";");
                else if (v.contains("Retard")) setStyle("-fx-text-fill:"+C_YELLOW+";");
                else                           setStyle("-fx-text-fill:"+C_TEXT2+";");
            }
        });

        table.getColumns().addAll(c1,c2,c3,c4,c5,c6);
        table.setItems(FXCollections.observableArrayList(aeroport.getVols().values()));
        return table;
    }

    // ═══════════════════════════════════════════════════════════
    //  COMPOSANTS DASHBOARD
    // ═══════════════════════════════════════════════════════════
    private VBox infoCard() {
        VBox c = card();
        long enService = aeroport.getFlotte().stream().filter(a -> !a.isEnMaintenance()).count();
        long enMaint   = aeroport.getFlotte().size() - enService;
        c.getChildren().add(mkLabel("✈  Avions en service", 11, C_TEXT2));
        c.getChildren().add(mkLabel(enService + " opérationnels  •  " + enMaint + " en maintenance", 14, C_TEXT));
        return c;
    }

    private VBox pistesCard() {
        VBox c = card();
        long libres   = aeroport.getPistes().stream().filter(Piste::isDisponible).count();
        long occupees = aeroport.getPistes().size() - libres;
        c.getChildren().add(mkLabel("🛣  État des pistes", 11, C_TEXT2));
        c.getChildren().add(mkLabel(libres + " libres  •  " + occupees + " occupées", 14, C_TEXT));
        return c;
    }

    private VBox kpiCard(String ico, String label, String val, String col, String sub) {
        VBox c = new VBox(6);
        c.setPrefWidth(180); c.setPadding(new Insets(18,20,18,20));
        c.setStyle("-fx-background-color:"+C_CARD+";"
                 + "-fx-background-radius:12;"
                 + "-fx-border-color:"+col+"30;"
                 + "-fx-border-width:1; -fx-border-radius:12;");
        c.getChildren().addAll(
            mkLabel(ico, 22, col),
            mkLabel(val, 28, col),
            mkLabel(label, 12, C_TEXT),
            mkLabel(sub,   10, C_TEXT3)
        );
        c.getChildren().get(1).setStyle(
            c.getChildren().get(1).getStyle() + "-fx-font-weight:bold; -fx-font-family:'Courier New';");
        c.setOnMouseEntered(e -> c.setStyle(
            "-fx-background-color:"+C_CARD+";"
          + "-fx-background-radius:12; -fx-border-color:"+col+";"
          + "-fx-border-width:1; -fx-border-radius:12;"
          + "-fx-effect:dropshadow(gaussian,"+col+"55,14,0,0,3);"));
        c.setOnMouseExited(e -> c.setStyle(
            "-fx-background-color:"+C_CARD+";"
          + "-fx-background-radius:12; -fx-border-color:"+col+"30;"
          + "-fx-border-width:1; -fx-border-radius:12;"));
        return c;
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS UI
    // ═══════════════════════════════════════════════════════════
    private VBox page() {
        VBox v = new VBox(18);
        v.setPadding(new Insets(30));
        return v;
    }

    private HBox pageTitle(String ico, String title, String sub) {
        VBox txt = new VBox(3);
        Label t = new Label(ico + "  " + title);
        t.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        Label s = new Label(sub);
        s.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_TEXT2 + ";");
        txt.getChildren().addAll(t, s);
        return new HBox(txt);
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT2 + ";"
                 + "-fx-padding:6 0 2 0;");
        return l;
    }

    private VBox card() {
        VBox c = new VBox(12);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:" + C_CARD + "; -fx-background-radius:10;");
        return c;
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(14); g.setVgap(10);
        return g;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        tf.setPrefWidth(160);
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-color:" + C_SURFACE + ";"
             + "-fx-text-fill:" + C_TEXT + ";"
             + "-fx-prompt-text-fill:" + C_TEXT3 + ";"
             + "-fx-border-color:" + C_BORDER + ";"
             + "-fx-border-radius:6; -fx-background-radius:6;"
             + "-fx-padding:7 10 7 10; -fx-font-size:12px;";
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + C_TEXT2 + "; -fx-font-size:11px;");
        GridPane.setMargin(l, new Insets(0, 6, 0, 0));
        return l;
    }

    private Label mkLabel(String text, int size, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:" + size + "px;");
        return l;
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";"
                 + "-fx-text-fill:#0D1117; -fx-font-size:12px; -fx-font-weight:bold;"
                 + "-fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 16 8 16;");
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color:" + color + "cc;"
          + "-fx-text-fill:#0D1117; -fx-font-size:12px; -fx-font-weight:bold;"
          + "-fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 16 8 16;"));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color:" + color + ";"
          + "-fx-text-fill:#0D1117; -fx-font-size:12px; -fx-font-weight:bold;"
          + "-fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 16 8 16;"));
        return b;
    }

    private Label msgLabel() {
        Label l = new Label();
        l.setStyle("-fx-font-size:11px;");
        return l;
    }

    private String msgStyle(String color) {
        return "-fx-font-size:11px; -fx-text-fill:" + color + ";";
    }

    private String tableStyle() {
        return "-fx-background-color:" + C_CARD + "; -fx-background-radius:10;"
             + "-fx-table-cell-border-color:transparent; -fx-font-size:12px;"
             + "-fx-text-fill:" + C_TEXT + ";";
    }

    private String comboStyle() {
        return "-fx-background-color:" + C_SURFACE + "; -fx-text-fill:" + C_TEXT + ";"
             + "-fx-border-color:" + C_BORDER + "; -fx-border-radius:6;"
             + "-fx-background-radius:6; -fx-font-size:12px;";
    }

    private <S> TableColumn<S,String> tc(String name, int w) {
        TableColumn<S,String> c = new TableColumn<>(name);
        c.setPrefWidth(w);
        return c;
    }

    private <S> void colorCell(TableColumn<S,String> col, String match, String colYes, String colNo) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle("-fx-text-fill:" + (v.contains(match) ? colYes : colNo) + ";"
                       + (v.contains(match) ? " -fx-font-weight:bold;" : ""));
            }
        });
    }

    private ScrollPane scrollOf(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        return sp;
    }

    private void swap(javafx.scene.Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
        mainContent.getChildren().setAll(node);
    }

    private void animateSidebarIn() {
        sidebar.setTranslateX(-220);
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(350),
            new KeyValue(sidebar.translateXProperty(), 0, Interpolator.EASE_OUT)));
        tl.play();
    }

    // ═══════════════════════════════════════════════════════════
    //  DONNÉES DE DÉMONSTRATION
    // ═══════════════════════════════════════════════════════════
    private void initDemoData() {
        aeroport = new GestionAeroport("Aéroport International de Tunis-Carthage");

        // Avions
        aeroport.ajouterAvion(new Avion("TU-JAL","Airbus A320",  180, 6100.0));
        aeroport.ajouterAvion(new Avion("TU-BOE","Boeing 737",   160, 5765.0));
        aeroport.ajouterAvion(new Avion("TU-LNG","Airbus A350",  350,15000.0));
        Avion a4 = new Avion("TU-REG","ATR 72", 72, 1500.0);
        aeroport.ajouterAvion(a4);
        a4.mettreEnMaintenance();

        // Pilotes
        aeroport.ajouterPilote(new Pilote("PIL001","Ben Ali",  "Karim",  "k.benali@airport.tn",  "LIC-001",3500,"pass123"));
        aeroport.ajouterPilote(new Pilote("PIL002","Meddeb",   "Sonia",  "s.meddeb@airport.tn",  "LIC-002",800, "pass456"));
        aeroport.ajouterPilote(new Pilote("PIL003","Trabelsi", "Ahmed",  "a.trabelsi@airport.tn","LIC-003",1200,"pass789"));

        // Passagers
        aeroport.ajouterPassager(new Passager("PAS001","Gharbi", "Leila",  "l.gharbi@gmail.com", "TN123456","Tunisienne"));
        aeroport.ajouterPassager(new Passager("PAS002","Slama",  "Mohamed","m.slama@gmail.com",  "TN789012","Tunisien"));
        aeroport.ajouterPassager(new Passager("PAS003","Dupont", "Claire", "c.dupont@gmail.fr",  "FR345678","Française",true));
        aeroport.ajouterPassager(new Passager("PAS004","Hassan", "Omar",   "o.hassan@gmail.com", "DZ567890","Algérien"));
        aeroport.ajouterPassager(new Passager("PAS005","Martin", "Sophie", "s.martin@gmail.fr",  "FR901234","Française"));

        // Pistes
        aeroport.ajouterPiste(new Piste("P01",3200.0));
        aeroport.ajouterPiste(new Piste("P02",2800.0));
        aeroport.ajouterPiste(new Piste("P03",1500.0));

        // Vols
        try {
            aeroport.creerVol("TU101","Tunis","Paris",   1700.0,"TU-JAL","PIL001");
            aeroport.creerVol("TU202","Tunis","Montréal",7500.0,"TU-LNG","PIL001");
            aeroport.creerVol("TU303","Tunis","Djerba",   400.0,"TU-BOE","PIL003");
        } catch (Exception ignored) {}

        // Réservations
        aeroport.reserverPassager("PAS001","TU101");
        aeroport.reserverPassager("PAS002","TU101");
        aeroport.reserverPassager("PAS003","TU202");

        // Statut + piste
        Vol tu101 = aeroport.getVols().get("TU101");
        if (tu101 != null) {
            tu101.changerStatut(StatutVol.EMBARQUEMENT);
            aeroport.getPistes().get(0).assignerVol(tu101);
        }
    }

    public static void main(String[] args) { launch(args); }
}