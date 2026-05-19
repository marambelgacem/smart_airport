package airport.ui;

import airport.collection.GestionAeroport;
import airport.exception.AvionEnMaintenanceException;
import airport.exception.PiloteNonQualifieException;
import airport.model.avion.Avion;
import airport.model.personnel.AgentSol;
import airport.model.personnel.Passager;
import airport.model.personnel.Personne;
import airport.model.personnel.Pilote;
import airport.model.service.Piste;
import airport.model.vol.StatutVol;
import airport.model.vol.Vol;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Smart Airport premium operations desk.
 */
public class AirportApp extends Application {

    private record SearchOption(String value, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record SearchSelector(VBox root, TextField searchField, ComboBox<SearchOption> combo, List<SearchOption> options) { }

    private GestionAeroport aeroport;

    private static final String T_BG = "#08111B";
    private static final String T_CANVAS = "#0F1824";
    private static final String T_PANEL = "#132031";
    private static final String T_PANEL_SOFT = "#172638";
    private static final String T_BORDER = "#2A394A";
    private static final String T_HAIR = "#4F6074";
    private static final String T_IVORY = "#EEE8DD";
    private static final String T_MUTED = "#AAB2BC";
    private static final String T_SUBTLE = "#6F7B86";
    private static final String T_BRASS = "#C8A05E";
    private static final String T_TEAL = "#70C0BC";
    private static final String T_SAGE = "#8DBDA3";
    private static final String T_RED = "#C67B7B";
    private static final String T_BLUE = "#89A7D8";
    private static final String T_CHARCOAL = "#0C141F";

    private static final String DISPLAY_FONT = "'Arial Narrow'";
    private static final String BODY_FONT = "'Segoe UI'";

    private BorderPane root;
    private StackPane mainContent;
    private VBox navRail;
    private Label clockLbl;
    private Button activeBtn;

    private Button navDashboard;
    private Button navFlights;
    private Button navFleet;
    private Button navPilots;
    private Button navPassengers;
    private Button navRunways;
    private Button navRoster;

    private String pageNoticeText;
    private String pageNoticeTone = T_IVORY;

    @Override
    public void start(Stage stage) {
        initDemoData();

        root = new BorderPane();
        root.setStyle(appShellStyle());

        root.setTop(buildTopBar());
        root.setLeft(buildNavRail());

        mainContent = new StackPane();
        mainContent.setPadding(new Insets(18, 18, 18, 0));
        mainContent.setStyle("-fx-background-color:transparent;");
        root.setCenter(mainContent);

        showDashboard();

        Scene scene = new Scene(root, 1380, 860);
        scene.setFill(Color.web(T_BG));
        stage.setTitle("SmartAirport — Operations Desk");
        stage.setScene(scene);
        stage.setMinWidth(1140);
        stage.setMinHeight(700);
        stage.show();

        animateNavRailIn();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(18);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(18, 24, 18, 24));
        bar.setStyle(
            "-fx-background-color:linear-gradient(to right, rgba(19,32,49,0.96), rgba(15,24,36,0.96));" +
            "-fx-border-color:rgba(200,160,94,0.18);" +
            "-fx-border-width:0 0 1 0;"
        );

        VBox brand = new VBox(2);
        Label eyebrow = overline("INTERNAL OPERATIONS DESK", T_BRASS);
        Label title = new Label("Smart Airport");
        title.setStyle(
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:26px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;" +
            "-fx-letter-spacing:1.4px;"
        );
        Label airportName = new Label(aeroport.getNomAeroport());
        airportName.setStyle(bodyMutedStyle(11));
        brand.getChildren().addAll(eyebrow, title, airportName);

        HBox quickActions = new HBox(8,
            quickAction("Flights", () -> openSection(navFlights, this::showVols)),
            quickAction("Runways", () -> openSection(navRunways, this::showPistes)),
            quickAction("Fleet", () -> openSection(navFleet, this::showAvions))
        );
        quickActions.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox liveBadge = buildLiveBadge();
        VBox timeBox = new VBox(3);
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        Label timeLabel = overline("LOCAL TIME", T_SUBTLE);
        clockLbl = new Label();
        clockLbl.setStyle(
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:15px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-font-weight:600;"
        );
        updateClock();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        timeBox.getChildren().addAll(timeLabel, clockLbl);

        bar.getChildren().addAll(brand, quickActions, spacer, liveBadge, timeBox);
        return bar;
    }

    private VBox buildNavRail() {
        navRail = new VBox(12);
        navRail.setPrefWidth(104);
        navRail.setPadding(new Insets(22, 14, 22, 16));
        navRail.setStyle(
            "-fx-background-color:linear-gradient(to bottom, rgba(8,17,27,0.94), rgba(12,20,31,0.94));" +
            "-fx-border-color:rgba(200,160,94,0.12);" +
            "-fx-border-width:0 1 0 0;"
        );

        VBox masthead = new VBox(4);
        masthead.setAlignment(Pos.CENTER_LEFT);
        Label code = new Label("TUN");
        code.setStyle(
            "-fx-text-fill:" + T_BRASS + ";" +
            "-fx-font-size:22px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;" +
            "-fx-letter-spacing:2px;"
        );
        Label role = new Label("air desk");
        role.setStyle(bodyMutedStyle(10));
        masthead.getChildren().addAll(code, role);

        navDashboard = navButton("OPS", "Board");
        navFlights = navButton("FLT", "Flights");
        navFleet = navButton("FLT", "Fleet");
        navPilots = navButton("AIR", "Pilots");
        navPassengers = navButton("PAX", "Guests");
        navRunways = navButton("RWY", "Runways");
        navRoster = navButton("ALL", "Roster");

        navDashboard.setOnAction(e -> openSection(navDashboard, this::showDashboard));
        navFlights.setOnAction(e -> openSection(navFlights, this::showVols));
        navFleet.setOnAction(e -> openSection(navFleet, this::showAvions));
        navPilots.setOnAction(e -> openSection(navPilots, this::showPilotes));
        navPassengers.setOnAction(e -> openSection(navPassengers, this::showPassagers));
        navRunways.setOnAction(e -> openSection(navRunways, this::showPistes));
        navRoster.setOnAction(e -> openSection(navRoster, this::showPersonnel));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button quit = new Button("Exit");
        quit.setMaxWidth(Double.MAX_VALUE);
        quit.setContentDisplay(ContentDisplay.CENTER);
        quit.setStyle(ghostButtonStyle(T_RED, true));
        quit.setOnMouseEntered(e -> quit.setStyle(ghostButtonStyle(T_RED, false)));
        quit.setOnMouseExited(e -> quit.setStyle(ghostButtonStyle(T_RED, true)));
        quit.setOnAction(e -> Platform.exit());

        navRail.getChildren().addAll(
            masthead, sectionSpacer(),
            navDashboard, navFlights, navFleet, navPilots, navPassengers, navRunways, navRoster,
            spacer, quit
        );
        setActive(navDashboard);
        return navRail;
    }

    private void showDashboard() {
        VBox page = page();
        page.getChildren().add(pageTitle("Operations lounge", "Live airport supervision with runway, fleet, and staffing context."));
        appendNotice(page);

        HBox heroRow = new HBox(18);
        VBox hero = buildLiveOperationsPanel();
        VBox board = buildFlightBoardPanel("Active departures board", "Current services across the airport network.", 6, true);
        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(board, Priority.ALWAYS);
        heroRow.getChildren().addAll(hero, board);

        HBox support = new HBox(18,
            buildSummaryPanel("Fleet balance", "Readiness, long-haul coverage, and maintenance watch.", fleetSummaryLines(), T_BRASS),
            buildSummaryPanel("Runway posture", "Availability and runway assignment health.", runwaySummaryLines(), T_TEAL),
            buildSummaryPanel("Personnel pulse", "Air crew, ground support, and guest movement.", personnelSummaryLines(), T_BLUE)
        );
        support.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        page.getChildren().addAll(heroRow, support);
        swap(scrollOf(page));
    }

    private void showVols() {
        VBox page = page();
        page.getChildren().add(pageTitle("Flight command desk", "Schedule services, manage reservations, and update operational states."));
        appendNotice(page);

        page.getChildren().add(buildMetricRow(
            metricCard("Scheduled", String.valueOf(aeroport.getVols().size()), "network services", T_BRASS),
            metricCard("Boarding", String.valueOf(countFlights(StatutVol.EMBARQUEMENT)), "active loading", T_TEAL),
            metricCard("Airborne", String.valueOf(countFlights(StatutVol.EN_VOL)), "in transit", T_BLUE)
        ));

        HBox layout = new HBox(18);
        VBox board = buildFlightBoardPanel("Traffic board", "Route status, pilot assignment, runway context, and load factor.", 100, false);
        board.setPrefWidth(760);
        VBox actions = new VBox(18,
            buildCreateFlightPanel(),
            buildReservationPanel(),
            buildStatusPanel()
        );
        actions.setPrefWidth(420);
        HBox.setHgrow(board, Priority.ALWAYS);
        layout.getChildren().addAll(board, actions);

        page.getChildren().add(layout);
        swap(scrollOf(page));
    }

    private void showAvions() {
        VBox page = page();
        page.getChildren().add(pageTitle("Fleet and maintenance", "Operational readiness across the aircraft portfolio."));
        appendNotice(page);

        long ready = aeroport.getFlotte().stream().filter(a -> !a.isEnMaintenance()).count();
        long inMaintenance = aeroport.getFlotte().size() - ready;
        long longHaulReady = aeroport.getFlotte().stream().filter(a -> a.peutEffectuerVol(5000)).count();

        page.getChildren().add(buildMetricRow(
            metricCard("Operational", String.valueOf(ready), "aircraft available", T_TEAL),
            metricCard("Maintenance", String.valueOf(inMaintenance), "aircraft in service bay", T_RED),
            metricCard("Long haul", String.valueOf(longHaulReady), "range cleared above 5000 km", T_BRASS)
        ));

        HBox layout = new HBox(18);
        VBox fleetGallery = premiumPanel("FLEET PROFILE", "Aircraft roster", "Readiness and capability at a glance.", T_BRASS);
        FlowPane fleetCards = new FlowPane(16, 16);
        fleetCards.setPrefWrapLength(760);
        for (Avion avion : aeroport.getFlotte()) {
            fleetCards.getChildren().add(buildAircraftCard(avion));
        }
        fleetGallery.getChildren().add(fleetCards);
        HBox.setHgrow(fleetGallery, Priority.ALWAYS);

        VBox actions = new VBox(18,
            buildAddAircraftPanel(),
            buildMaintenancePanel()
        );
        actions.setPrefWidth(420);

        layout.getChildren().addAll(fleetGallery, actions);
        page.getChildren().add(layout);
        swap(scrollOf(page));
    }

    private void showPilotes() {
        VBox page = page();
        page.getChildren().add(pageTitle("Air crew registry", "Pilot qualification, contact profile, and capability overview."));
        appendNotice(page);

        long qualified = aeroport.getPilotes().stream().filter(Pilote::estQualifieLongCourrier).count();
        page.getChildren().add(buildMetricRow(
            metricCard("Registered", String.valueOf(aeroport.getPilotes().size()), "pilots on record", T_BRASS),
            metricCard("Long haul cleared", String.valueOf(qualified), "crew ready for extended sectors", T_TEAL)
        ));

        HBox layout = new HBox(18);
        VBox tablePanel = premiumPanel("PILOT ROSTER", "Crew overview", "Operational aircrew with long-haul readiness and contact details.", T_TEAL);
        TableView<Pilote> table = buildPilotsTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tablePanel.getChildren().add(table);
        HBox.setHgrow(tablePanel, Priority.ALWAYS);

        VBox formPanel = buildAddPilotPanel();
        formPanel.setPrefWidth(420);

        layout.getChildren().addAll(tablePanel, formPanel);
        page.getChildren().add(layout);
        swap(scrollOf(page));
    }

    private void showPassagers() {
        VBox page = page();
        page.getChildren().add(pageTitle("Passenger registry", "Guest records, priority status, and travel identity management."));
        appendNotice(page);

        long priority = aeroport.getPassagers().values().stream().filter(Passager::isPassagerPrioritaire).count();
        page.getChildren().add(buildMetricRow(
            metricCard("Registered", String.valueOf(aeroport.getPassagers().size()), "travellers on file", T_BRASS),
            metricCard("Priority", String.valueOf(priority), "priority service guests", T_SAGE)
        ));

        HBox layout = new HBox(18);
        VBox tablePanel = premiumPanel("PASSENGER FILES", "Guest overview", "Identity, nationality, and priority service visibility.", T_BRASS);
        TableView<Passager> table = buildPassengersTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tablePanel.getChildren().add(table);
        HBox.setHgrow(tablePanel, Priority.ALWAYS);

        VBox formPanel = buildAddPassengerPanel();
        formPanel.setPrefWidth(420);

        layout.getChildren().addAll(tablePanel, formPanel);
        page.getChildren().add(layout);
        swap(scrollOf(page));
    }

    private void showPistes() {
        VBox page = page();
        page.getChildren().add(pageTitle("Runway control", "Visual runway posture, allocation, and release management."));
        appendNotice(page);

        page.getChildren().add(buildMetricRow(
            metricCard("Runways", String.valueOf(aeroport.getPistes().size()), "total strips", T_BRASS),
            metricCard("Available", String.valueOf(aeroport.getPistes().stream().filter(Piste::isDisponible).count()), "ready for assignment", T_TEAL),
            metricCard("Occupied", String.valueOf(aeroport.getPistes().stream().filter(p -> !p.isDisponible()).count()), "currently engaged", T_RED)
        ));

        VBox runwayPanel = premiumPanel("RUNWAY MAP", "Operational strip view", "Runway occupancy, assigned service, and strip length.", T_TEAL);
        VBox runwayList = new VBox(12);
        for (Piste piste : aeroport.getPistes()) {
            runwayList.getChildren().add(buildRunwayStrip(piste));
        }
        runwayPanel.getChildren().add(runwayList);

        HBox actions = new HBox(18, buildAddRunwayPanel(), buildRunwayActionPanel());
        actions.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        page.getChildren().addAll(runwayPanel, actions);
        swap(scrollOf(page));
    }

    private void showPersonnel() {
        VBox page = page();
        page.getChildren().add(pageTitle("Unified roster", "One operational view across pilots, ground staff, and passenger records."));
        appendNotice(page);

        Label note = new Label(
            "This desk aggregates all registered people into one operational roster. " +
            "The role column is resolved dynamically from each specific type."
        );
        note.setWrapText(true);
        note.setStyle(
            "-fx-text-fill:" + T_MUTED + ";" +
            "-fx-font-size:12px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-background-color:rgba(111,192,188,0.08);" +
            "-fx-background-radius:16;" +
            "-fx-border-color:rgba(111,192,188,0.16);" +
            "-fx-border-radius:16;" +
            "-fx-padding:14 18 14 18;"
        );

        VBox tablePanel = premiumPanel("UNIFIED ROSTER", "People and roles", "Polymorphic operational roster across air, ground, and guest records.", T_BLUE);
        TableView<Personne> table = buildPersonnelTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tablePanel.getChildren().add(table);

        page.getChildren().addAll(note, tablePanel);
        swap(scrollOf(page));
    }

    private VBox buildLiveOperationsPanel() {
        VBox panel = premiumPanel("LIVE OPERATIONS", "Airport command posture", "Immediate picture of active traffic, runway load, and service readiness.", T_BRASS);

        HBox emphasis = new HBox(18,
            heroValue("Active flights", String.valueOf(activeFlightsCount()), T_IVORY),
            heroValue("Runways in use", String.valueOf(aeroport.getPistes().stream().filter(p -> !p.isDisponible()).count()), T_TEAL),
            heroValue("Available fleet", String.valueOf(aeroport.getFlotte().stream().filter(a -> !a.isEnMaintenance()).count()), T_SAGE)
        );
        emphasis.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        VBox highlights = new VBox(10,
            operationLine("Current primary service", spotlightFlightText()),
            operationLine("Runway assignment", occupiedRunwayText()),
            operationLine("Long-haul readiness", longHaulReadinessText()),
            operationLine("Priority guest movement", priorityPassengerText())
        );

        HBox support = new HBox(12,
            slimInfoCard("Maintenance watch", maintenanceWatchText(), T_RED),
            slimInfoCard("Crew posture", crewPostureText(), T_BLUE)
        );
        support.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        panel.getChildren().addAll(emphasis, divider(), highlights, support);
        return panel;
    }

    private VBox buildFlightBoardPanel(String eyebrow, String title, int maxRows, boolean compact) {
        VBox panel = premiumPanel("TRAFFIC BOARD", eyebrow, title, T_TEAL);
        panel.getChildren().add(boardHeader(compact));

        VBox rows = new VBox(8);
        List<Vol> vols = new ArrayList<>(aeroport.getVols().values());
        vols.sort(Comparator.comparing(Vol::getNumeroVol));
        for (int i = 0; i < Math.min(maxRows, vols.size()); i++) {
            rows.getChildren().add(boardRow(vols.get(i), compact));
        }

        if (vols.isEmpty()) {
            rows.getChildren().add(emptyState("No flights registered yet."));
        }

        panel.getChildren().add(rows);
        return panel;
    }

    private VBox buildSummaryPanel(String title, String caption, List<String> lines, String accent) {
        VBox panel = premiumPanel("OPERATIONS SUMMARY", title, caption, accent);
        for (String line : lines) {
            panel.getChildren().add(operationLine("•", line));
        }
        return panel;
    }

    private VBox buildCreateFlightPanel() {
        VBox panel = premiumPanel("SCHEDULE SERVICE", "Create flight", "Register a new service with aircraft and pilot assignment.", T_BRASS);
        GridPane grid = grid();

        TextField tfNum = field("ex: TU105");
        TextField tfDep = field("ex: Tunis");
        TextField tfArr = field("ex: Paris");
        TextField tfDist = field("ex: 1700");
        SearchSelector cbImmat = searchableSelector(
            aeroport.getFlotte(),
            Avion::getImmatriculation,
            a -> a.getImmatriculation() + " · " + a.getModele(),
            "Search aircraft"
        );
        SearchSelector cbPilot = searchableSelector(
            new ArrayList<>(aeroport.getPilotes()),
            Pilote::getId,
            p -> p.getId() + " · " + p.getPrenom() + " " + p.getNom(),
            "Search pilot"
        );

        grid.add(fieldLabel("Flight"), 0, 0); grid.add(tfNum, 1, 0);
        grid.add(fieldLabel("Origin"), 2, 0); grid.add(tfDep, 3, 0);
        grid.add(fieldLabel("Destination"), 0, 1); grid.add(tfArr, 1, 1);
        grid.add(fieldLabel("Distance"), 2, 1); grid.add(tfDist, 3, 1);
        grid.add(fieldLabel("Aircraft"), 0, 2); grid.add(cbImmat.root(), 1, 2);
        grid.add(fieldLabel("Pilot"), 2, 2); grid.add(cbPilot.root(), 3, 2);

        Button create = primaryButton("Register service", T_BRASS);
        create.setOnAction(e -> {
            try {
                double distance = Double.parseDouble(tfDist.getText().trim());
                aeroport.creerVol(
                    tfNum.getText().trim(),
                    tfDep.getText().trim(),
                    tfArr.getText().trim(),
                    distance,
                    selectedValue(cbImmat),
                    selectedValue(cbPilot)
                );
                flashNotice("Flight " + tfNum.getText().trim() + " registered.", T_SAGE);
            } catch (AvionEnMaintenanceException ex) {
                flashNotice(ex.getMessage(), T_RED);
            } catch (PiloteNonQualifieException ex) {
                flashNotice(ex.getMessage(), T_RED);
            } catch (Exception ex) {
                flashNotice(ex.getMessage(), T_RED);
            }
            showVols();
        });

        panel.getChildren().addAll(grid, create);
        return panel;
    }

    private VBox buildReservationPanel() {
        VBox panel = premiumPanel("GUEST MOVEMENT", "Reserve passenger", "Assign a registered passenger to an available service.", T_TEAL);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        SearchSelector cbPassenger = searchableSelector(
            new ArrayList<>(aeroport.getPassagers().values()),
            Passager::getId,
            p -> p.getId() + " · " + p.getPrenom() + " " + p.getNom(),
            "Search passenger"
        );
        SearchSelector cbFlight = searchableSelector(
            new ArrayList<>(aeroport.getVols().values()),
            Vol::getNumeroVol,
            v -> v.getNumeroVol() + " · " + v.getVilleDepart() + " → " + v.getVilleArrivee(),
            "Search flight"
        );
        Button reserve = primaryButton("Reserve seat", T_TEAL);
        reserve.setOnAction(e -> {
            String result = aeroport.reserverPassager(selectedValue(cbPassenger), selectedValue(cbFlight));
            flashNotice(result, result.startsWith("✅") ? T_SAGE : T_RED);
            showVols();
        });
        row.getChildren().addAll(fieldLabel("Passenger"), cbPassenger.root(), fieldLabel("Flight"), cbFlight.root());
        panel.getChildren().addAll(row, reserve);
        return panel;
    }

    private VBox buildStatusPanel() {
        VBox panel = premiumPanel("TRAFFIC STATUS", "Update service state", "Move an existing service through the live operational timeline.", T_BLUE);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        SearchSelector cbFlight = searchableSelector(
            new ArrayList<>(aeroport.getVols().values()),
            Vol::getNumeroVol,
            v -> v.getNumeroVol() + " · " + v.getVilleDepart() + " → " + v.getVilleArrivee(),
            "Search flight"
        );
        ComboBox<StatutVol> combo = new ComboBox<>(FXCollections.observableArrayList(StatutVol.values()));
        combo.setStyle(comboStyle());
        combo.setPrefWidth(170);
        combo.getSelectionModel().selectFirst();
        Button update = primaryButton("Update status", T_BLUE);
        update.setOnAction(e -> {
            String flightNumber = selectedValue(cbFlight);
            Vol vol = aeroport.getVols().get(flightNumber);
            if (vol == null) {
                flashNotice("Flight not found: " + flightNumber, T_RED);
            } else {
                vol.changerStatut(combo.getValue());
                flashNotice("Status updated for " + vol.getNumeroVol() + ".", T_SAGE);
            }
            showVols();
        });
        row.getChildren().addAll(fieldLabel("Flight"), cbFlight.root(), combo);
        panel.getChildren().addAll(row, update);
        return panel;
    }

    private VBox buildAddAircraftPanel() {
        VBox panel = premiumPanel("FLEET ENTRY", "Add aircraft", "Register a new aircraft with seat and range profile.", T_BRASS);
        GridPane grid = grid();

        TextField tfIm = field("ex: TU-NEO");
        TextField tfMo = field("ex: Airbus A321");
        TextField tfCa = field("ex: 220");
        TextField tfAu = field("ex: 7000");

        grid.add(fieldLabel("Registration"), 0, 0); grid.add(tfIm, 1, 0);
        grid.add(fieldLabel("Model"), 2, 0); grid.add(tfMo, 3, 0);
        grid.add(fieldLabel("Seats"), 0, 1); grid.add(tfCa, 1, 1);
        grid.add(fieldLabel("Range km"), 2, 1); grid.add(tfAu, 3, 1);

        Button add = primaryButton("Add aircraft", T_BRASS);
        add.setOnAction(e -> {
            try {
                aeroport.ajouterAvion(new Avion(
                    tfIm.getText().trim(),
                    tfMo.getText().trim(),
                    Integer.parseInt(tfCa.getText().trim()),
                    Double.parseDouble(tfAu.getText().trim())
                ));
                flashNotice("Aircraft " + tfIm.getText().trim() + " added to fleet.", T_SAGE);
            } catch (Exception ex) {
                flashNotice(ex.getMessage(), T_RED);
            }
            showAvions();
        });

        panel.getChildren().addAll(grid, add);
        return panel;
    }

    private VBox buildMaintenancePanel() {
        VBox panel = premiumPanel("SERVICE BAY", "Maintenance control", "Move aircraft in and out of maintenance posture.", T_RED);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        SearchSelector cbMaint = searchableSelector(
            aeroport.getFlotte(),
            Avion::getImmatriculation,
            a -> a.getImmatriculation() + " · " + a.getModele(),
            "Search aircraft"
        );
        Button on = primaryButton("Set maintenance", T_RED);
        Button off = secondaryButton("Return to service", T_SAGE);

        on.setOnAction(e -> aeroport.trouverAvion(selectedValue(cbMaint)).ifPresentOrElse(a -> {
            a.mettreEnMaintenance();
            flashNotice(a.getImmatriculation() + " moved to maintenance.", T_RED);
            showAvions();
        }, () -> {
            flashNotice("Aircraft not found: " + selectedValue(cbMaint), T_RED);
            showAvions();
        }));

        off.setOnAction(e -> aeroport.trouverAvion(selectedValue(cbMaint)).ifPresentOrElse(a -> {
            a.sortirDeMaintenance();
            flashNotice(a.getImmatriculation() + " returned to service.", T_SAGE);
            showAvions();
        }, () -> {
            flashNotice("Aircraft not found: " + selectedValue(cbMaint), T_RED);
            showAvions();
        }));

        row.getChildren().addAll(fieldLabel("Aircraft"), cbMaint.root());
        panel.getChildren().addAll(row, new HBox(10, on, off));
        return panel;
    }

    private VBox buildAddPilotPanel() {
        VBox panel = premiumPanel("CREW ENTRY", "Register pilot", "Add a new pilot profile with credentials and flight-hour record.", T_TEAL);
        GridPane grid = grid();

        TextField tfId = field("ex: PIL004");
        TextField tfNom = field("Nom");
        TextField tfPrenom = field("Prenom");
        TextField tfEmail = field("email@airport.tn");
        TextField tfLicence = field("ex: LIC-004");
        TextField tfHours = field("ex: 500");
        PasswordField tfMdp = new PasswordField();
        tfMdp.setPromptText("Mot de passe");
        tfMdp.setStyle(fieldStyle());

        grid.add(fieldLabel("ID"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(fieldLabel("Nom"), 2, 0); grid.add(tfNom, 3, 0);
        grid.add(fieldLabel("Prenom"), 0, 1); grid.add(tfPrenom, 1, 1);
        grid.add(fieldLabel("Email"), 2, 1); grid.add(tfEmail, 3, 1);
        grid.add(fieldLabel("Licence"), 0, 2); grid.add(tfLicence, 1, 2);
        grid.add(fieldLabel("Heures"), 2, 2); grid.add(tfHours, 3, 2);
        grid.add(fieldLabel("Mot de passe"), 0, 3); grid.add(tfMdp, 1, 3);

        Button add = primaryButton("Register pilot", T_TEAL);
        add.setOnAction(e -> {
            try {
                aeroport.ajouterPilote(new Pilote(
                    tfId.getText().trim(),
                    tfNom.getText().trim(),
                    tfPrenom.getText().trim(),
                    tfEmail.getText().trim(),
                    tfLicence.getText().trim(),
                    Integer.parseInt(tfHours.getText().trim()),
                    tfMdp.getText()
                ));
                flashNotice("Pilot " + tfId.getText().trim() + " registered.", T_SAGE);
            } catch (Exception ex) {
                flashNotice(ex.getMessage(), T_RED);
            }
            showPilotes();
        });

        panel.getChildren().addAll(grid, add);
        return panel;
    }

    private VBox buildAddPassengerPanel() {
        VBox panel = premiumPanel("GUEST ENTRY", "Register passenger", "Add a new passenger profile with travel identity and service priority.", T_BRASS);
        GridPane grid = grid();

        TextField tfId = field("ex: PAS006");
        TextField tfNom = field("Nom");
        TextField tfPrenom = field("Prenom");
        TextField tfEmail = field("email@gmail.com");
        TextField tfPassport = field("ex: TN999999");
        TextField tfNation = field("ex: Tunisien");
        CheckBox priority = new CheckBox("Priority service");
        priority.setStyle(
            "-fx-text-fill:" + T_MUTED + ";" +
            "-fx-font-size:12px;" +
            "-fx-font-family:" + BODY_FONT + ";"
        );

        grid.add(fieldLabel("ID"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(fieldLabel("Nom"), 2, 0); grid.add(tfNom, 3, 0);
        grid.add(fieldLabel("Prenom"), 0, 1); grid.add(tfPrenom, 1, 1);
        grid.add(fieldLabel("Email"), 2, 1); grid.add(tfEmail, 3, 1);
        grid.add(fieldLabel("Passeport"), 0, 2); grid.add(tfPassport, 1, 2);
        grid.add(fieldLabel("Nationalite"), 2, 2); grid.add(tfNation, 3, 2);
        grid.add(priority, 0, 3, 2, 1);

        Button add = primaryButton("Register passenger", T_BRASS);
        add.setOnAction(e -> {
            try {
                aeroport.ajouterPassager(new Passager(
                    tfId.getText().trim(),
                    tfNom.getText().trim(),
                    tfPrenom.getText().trim(),
                    tfEmail.getText().trim(),
                    tfPassport.getText().trim(),
                    tfNation.getText().trim(),
                    priority.isSelected()
                ));
                flashNotice("Passenger " + tfId.getText().trim() + " registered.", T_SAGE);
            } catch (Exception ex) {
                flashNotice(ex.getMessage(), T_RED);
            }
            showPassagers();
        });

        panel.getChildren().addAll(grid, add);
        return panel;
    }

    private VBox buildAddRunwayPanel() {
        VBox panel = premiumPanel("INFRASTRUCTURE", "Add runway", "Register a new runway strip and its usable length.", T_BRASS);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        TextField tfNumber = field("Runway code");
        TextField tfLength = field("Length m");
        row.getChildren().addAll(fieldLabel("Runway"), tfNumber, fieldLabel("Length"), tfLength);

        Button add = primaryButton("Register runway", T_BRASS);
        add.setOnAction(e -> {
            try {
                aeroport.ajouterPiste(new Piste(tfNumber.getText().trim(), Double.parseDouble(tfLength.getText().trim())));
                flashNotice("Runway " + tfNumber.getText().trim() + " registered.", T_SAGE);
            } catch (Exception ex) {
                flashNotice(ex.getMessage(), T_RED);
            }
            showPistes();
        });

        panel.getChildren().addAll(row, add);
        return panel;
    }

    private VBox buildRunwayActionPanel() {
        VBox panel = premiumPanel("STRIP CONTROL", "Assign or release", "Bind a service to a runway or return the strip to available status.", T_TEAL);
        GridPane grid = grid();
        SearchSelector cbRunway = searchableSelector(
            aeroport.getPistes(),
            Piste::getNumeroPiste,
            p -> p.getNumeroPiste() + " · " + (int) p.getLongueurMetres() + " m",
            "Search runway"
        );
        SearchSelector cbFlight = searchableSelector(
            new ArrayList<>(aeroport.getVols().values()),
            Vol::getNumeroVol,
            v -> v.getNumeroVol() + " · " + v.getVilleDepart() + " → " + v.getVilleArrivee(),
            "Search flight"
        );
        Button assign = primaryButton("Assign flight", T_TEAL);
        Button release = secondaryButton("Release runway", T_BLUE);

        assign.setOnAction(e -> {
            String flightNumber = selectedValue(cbFlight);
            String runwayCode = selectedValue(cbRunway);
            Vol vol = aeroport.getVols().get(flightNumber);
            if (vol == null) {
                flashNotice("Flight not found: " + flightNumber, T_RED);
                showPistes();
                return;
            }
            aeroport.getPistes().stream()
                .filter(p -> p.getNumeroPiste().equals(runwayCode))
                .findFirst()
                .ifPresentOrElse(p -> {
                    boolean assigned = p.assignerVol(vol);
                    flashNotice(
                        assigned ? "Runway " + p.getNumeroPiste() + " assigned to " + vol.getNumeroVol() + "." : "Runway already occupied.",
                        assigned ? T_SAGE : T_RED
                    );
                    showPistes();
                }, () -> {
                    flashNotice("Runway not found: " + runwayCode, T_RED);
                    showPistes();
                });
        });

        release.setOnAction(e -> aeroport.getPistes().stream()
            .filter(p -> p.getNumeroPiste().equals(selectedValue(cbRunway)))
            .findFirst()
            .ifPresentOrElse(p -> {
                p.libererPiste();
                flashNotice("Runway " + p.getNumeroPiste() + " released.", T_SAGE);
                showPistes();
            }, () -> {
                flashNotice("Runway not found: " + selectedValue(cbRunway), T_RED);
                showPistes();
            }));

        grid.add(fieldLabel("Runway"), 0, 0); grid.add(cbRunway.root(), 1, 0);
        grid.add(fieldLabel("Flight"), 2, 0); grid.add(cbFlight.root(), 3, 0);
        panel.getChildren().addAll(grid, new HBox(10, assign, release));
        return panel;
    }

    private TableView<Pilote> buildPilotsTable() {
        TableView<Pilote> table = new TableView<>();
        table.setStyle(tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Pilote, String> c1 = tableColumn("ID", 90);
        TableColumn<Pilote, String> c2 = tableColumn("Nom complet", 180);
        TableColumn<Pilote, String> c3 = tableColumn("Licence", 120);
        TableColumn<Pilote, String> c4 = tableColumn("Heures", 110);
        TableColumn<Pilote, String> c5 = tableColumn("Email", 220);
        TableColumn<Pilote, String> c6 = tableColumn("Long-courrier", 150);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom() + " " + d.getValue().getNom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLicencePilote()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeuresDeVol() + " h"));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        c6.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estQualifieLongCourrier() ? "Cleared" : "Restricted"));
        chipCell(c6, "Cleared", T_SAGE, T_BRASS);

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6);
        table.setItems(FXCollections.observableArrayList(aeroport.getPilotes()));
        table.setPrefHeight(500);
        return table;
    }

    private TableView<Passager> buildPassengersTable() {
        TableView<Passager> table = new TableView<>();
        table.setStyle(tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Passager, String> c1 = tableColumn("ID", 90);
        TableColumn<Passager, String> c2 = tableColumn("Nom complet", 180);
        TableColumn<Passager, String> c3 = tableColumn("Passeport", 140);
        TableColumn<Passager, String> c4 = tableColumn("Nationalite", 150);
        TableColumn<Passager, String> c5 = tableColumn("Email", 220);
        TableColumn<Passager, String> c6 = tableColumn("Service", 140);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom() + " " + d.getValue().getNom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroPasport()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNationalite()));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        c6.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isPassagerPrioritaire() ? "Priority" : "Standard"));
        chipCell(c6, "Priority", T_BRASS, T_MUTED);

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6);
        table.setItems(FXCollections.observableArrayList(aeroport.getPassagers().values()));
        table.setPrefHeight(500);
        return table;
    }

    private TableView<Personne> buildPersonnelTable() {
        TableView<Personne> table = new TableView<>();
        table.setStyle(tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Personne, String> c1 = tableColumn("ID", 90);
        TableColumn<Personne, String> c2 = tableColumn("Prenom", 130);
        TableColumn<Personne, String> c3 = tableColumn("Nom", 130);
        TableColumn<Personne, String> c4 = tableColumn("Role", 220);
        TableColumn<Personne, String> c5 = tableColumn("Email", 260);

        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        c4.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                String tone = T_MUTED;
                if (value.startsWith("Pilote")) tone = T_TEAL;
                else if (value.startsWith("Agent")) tone = T_BRASS;
                else if (value.startsWith("Passager")) tone = T_SAGE;
                setStyle(
                    "-fx-background-color:rgba(19,32,49,0.92);" +
                    "-fx-text-fill:" + tone + ";" +
                    "-fx-font-size:11px;" +
                    "-fx-font-family:" + BODY_FONT + ";" +
                    "-fx-font-weight:600;" +
                    "-fx-padding:6 10 6 10;"
                );
            }
        });

        table.getColumns().addAll(c1, c2, c3, c4, c5);
        table.setItems(FXCollections.observableArrayList(aeroport.getToutLePersonnel()));
        table.setPrefHeight(560);
        return table;
    }

    private VBox buildAircraftCard(Avion avion) {
        String accent = avion.isEnMaintenance() ? T_RED : T_TEAL;
        VBox card = new VBox(10);
        card.setPrefWidth(230);
        card.setPadding(new Insets(18));
        card.setStyle(panelStyle(accent, true));

        Label registration = new Label(avion.getImmatriculation());
        registration.setStyle(
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:22px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;" +
            "-fx-letter-spacing:1.2px;"
        );
        Label model = new Label(avion.getModele());
        model.setStyle(bodyStyle(13, T_MUTED, false));

        HBox stateRow = new HBox(8, stateChip(avion.isEnMaintenance() ? "Maintenance" : "Operational", accent), runwayCode(avion.peutEffectuerVol(5000) ? "LONG" : "REG"));
        stateRow.setAlignment(Pos.CENTER_LEFT);

        VBox specs = new VBox(6,
            specLine("Seats", avion.getCapacitePassagers() + " pax"),
            specLine("Range", (int) avion.getAutonomieKm() + " km"),
            specLine("Capability", avion.peutEffectuerVol(5000) ? "Long-haul ready" : "Regional profile")
        );

        card.getChildren().addAll(registration, model, stateRow, divider(), specs);
        return card;
    }

    private VBox buildRunwayStrip(Piste piste) {
        boolean available = piste.isDisponible();
        String accent = available ? T_TEAL : T_RED;

        VBox strip = new VBox(10);
        strip.setPadding(new Insets(18, 18, 16, 18));
        strip.setStyle(panelStyle(accent, false));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox names = new VBox(2);
        Label title = new Label("Runway " + piste.getNumeroPiste());
        title.setStyle(
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:20px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;"
        );
        Label sub = new Label((int) piste.getLongueurMetres() + " m strip length");
        sub.setStyle(bodyMutedStyle(11));
        names.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox state = new HBox(8, stateChip(available ? "Available" : "Occupied", accent));
        if (!available && piste.getVolEnCours() != null) {
            state.getChildren().add(runwayCode(piste.getVolEnCours().getNumeroVol()));
        }
        state.setAlignment(Pos.CENTER_RIGHT);

        Region runwayGraphic = new Region();
        runwayGraphic.setPrefHeight(18);
        runwayGraphic.setStyle(
            "-fx-background-color:linear-gradient(to right, rgba(12,20,31,0.95), rgba(19,32,49,0.95));" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + accent + "55;" +
            "-fx-border-radius:12;" +
            "-fx-border-width:1;" +
            "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.22), 10, 0, 0, 3);"
        );

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Label usage = new Label(available ? "Ready for departure or landing assignment." : "Assigned to " + piste.getVolEnCours().getNumeroVol() + ".");
        usage.setStyle(bodyStyle(12, T_MUTED, false));
        footer.getChildren().add(usage);

        header.getChildren().addAll(names, spacer, state);
        strip.getChildren().addAll(header, runwayGraphic, footer);
        return strip;
    }

    private HBox boardHeader(boolean compact) {
        HBox header = new HBox(16);
        header.setPadding(new Insets(4, 12, 6, 12));
        header.setStyle(
            "-fx-border-color:rgba(200,160,94,0.14);" +
            "-fx-border-width:0 0 1 0;"
        );
        header.getChildren().addAll(
            boardLabel("Flight", compact ? 90 : 100),
            boardLabel("Route", compact ? 220 : 250),
            boardLabel("Pilot", compact ? 140 : 160),
            boardLabel("Runway", compact ? 90 : 100),
            boardLabel("Status", compact ? 120 : 130),
            boardLabel("Load", compact ? 90 : 100)
        );
        return header;
    }

    private HBox boardRow(Vol vol, boolean compact) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle(
            "-fx-background-color:rgba(255,255,255,0.02);" +
            "-fx-background-radius:14;" +
            "-fx-border-color:rgba(255,255,255,0.04);" +
            "-fx-border-radius:14;"
        );

        row.getChildren().addAll(
            boardCell(vol.getNumeroVol(), compact ? 90 : 100, T_IVORY, true),
            boardCell(vol.getVilleDepart() + " → " + vol.getVilleArrivee(), compact ? 220 : 250, T_MUTED, false),
            boardCell(vol.getPilote().getPrenom() + " " + vol.getPilote().getNom(), compact ? 140 : 160, T_IVORY, false),
            fixedNode(runwayCode(assignedRunway(vol)), compact ? 90 : 100),
            fixedNode(stateChip(statusLabel(vol.getStatut()), statusTone(vol.getStatut())), compact ? 120 : 130),
            boardCell(loadLabel(vol), compact ? 90 : 100, loadTone(vol), true)
        );
        return row;
    }

    private HBox buildMetricRow(VBox... cards) {
        HBox row = new HBox(16);
        for (VBox card : cards) {
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
        return row;
    }

    private VBox metricCard(String label, String value, String detail, String accent) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle(panelStyle(accent, true));
        card.getChildren().addAll(
            overline(label.toUpperCase(), accent),
            bigMetric(value, accent),
            labelText(detail)
        );
        return card;
    }

    private VBox premiumPanel(String eyebrow, String title, String caption, String accent) {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(20, 22, 20, 22));
        panel.setStyle(panelStyle(accent, false));

        VBox header = new VBox(4);
        header.getChildren().addAll(
            overline(eyebrow, accent),
            sectionTitle(title),
            labelText(caption)
        );

        panel.getChildren().add(header);
        return panel;
    }

    private VBox page() {
        VBox page = new VBox(18);
        page.setPadding(new Insets(22, 24, 28, 24));
        page.setStyle(
            "-fx-background-color:linear-gradient(to bottom, rgba(15,24,36,0.96), rgba(8,17,27,0.98));" +
            "-fx-background-radius:28;" +
            "-fx-border-color:rgba(255,255,255,0.04);" +
            "-fx-border-radius:28;" +
            "-fx-border-width:1;"
        );
        return page;
    }

    private VBox pageTitle(String title, String caption) {
        VBox box = new VBox(5);
        box.getChildren().addAll(
            overline("AIRPORT OPERATIONS", T_BRASS),
            sectionTitle(title),
            labelText(caption)
        );
        return box;
    }

    private void appendNotice(VBox page) {
        if (pageNoticeText == null || pageNoticeText.isBlank()) {
            return;
        }
        Label notice = new Label(pageNoticeText.replaceFirst("^[✅❌⚠]+\\s*", ""));
        notice.setWrapText(true);
        notice.setStyle(
            "-fx-text-fill:" + pageNoticeTone + ";" +
            "-fx-font-size:12px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-background-color:rgba(19,32,49,0.84);" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + pageNoticeTone + "44;" +
            "-fx-border-radius:14;" +
            "-fx-padding:10 14 10 14;"
        );
        page.getChildren().add(1, notice);
        pageNoticeText = null;
    }

    private GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        tf.setPrefWidth(160);
        return tf;
    }

    private <T> SearchSelector searchableSelector(List<T> items, Function<T, String> valueFn, Function<T, String> labelFn, String prompt) {
        List<SearchOption> options = items.stream()
            .map(item -> new SearchOption(valueFn.apply(item), labelFn.apply(item)))
            .sorted(Comparator.comparing(SearchOption::label, String.CASE_INSENSITIVE_ORDER))
            .toList();

        TextField searchField = field(prompt);
        searchField.setPrefWidth(210);
        ComboBox<SearchOption> combo = new ComboBox<>(FXCollections.observableArrayList(options));
        combo.setPromptText("Choose result");
        combo.setVisibleRowCount(Math.min(8, Math.max(4, options.size())));
        combo.setPrefWidth(210);
        combo.setStyle(comboStyle());
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SearchOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SearchOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choose result" : item.label());
            }
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            List<SearchOption> filtered = options.stream()
                .filter(option -> query.isBlank()
                    || option.label().toLowerCase().contains(query)
                    || option.value().toLowerCase().contains(query))
                .toList();

            SearchOption selected = combo.getSelectionModel().getSelectedItem();
            combo.getItems().setAll(filtered);

            if (query.isBlank()) {
                if (selected != null && options.contains(selected)) {
                    combo.getSelectionModel().select(selected);
                }
                return;
            }

            filtered.stream()
                .filter(option -> option.label().equalsIgnoreCase(query) || option.value().equalsIgnoreCase(query))
                .findFirst()
                .ifPresentOrElse(combo.getSelectionModel()::select, () -> {
                    if (selected != null && filtered.contains(selected)) {
                        combo.getSelectionModel().select(selected);
                    } else {
                        combo.getSelectionModel().clearSelection();
                    }
                });

            if (!filtered.isEmpty()) {
                combo.show();
            }
        });

        VBox wrapper = new VBox(6, searchField, combo);
        wrapper.setFillWidth(true);
        return new SearchSelector(wrapper, searchField, combo, options);
    }

    private String selectedValue(SearchSelector selector) {
        SearchOption selected = selector.combo().getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected.value();
        }
        String typed = selector.searchField().getText();
        if (typed == null) {
            return "";
        }
        String query = typed.trim();
        return selector.options().stream()
            .filter(option -> option.label().equalsIgnoreCase(query) || option.value().equalsIgnoreCase(query))
            .map(SearchOption::value)
            .findFirst()
            .orElse(query);
    }

    private String fieldStyle() {
        return "-fx-background-color:rgba(8,17,27,0.85);" +
               "-fx-text-fill:" + T_IVORY + ";" +
               "-fx-prompt-text-fill:" + T_SUBTLE + ";" +
               "-fx-border-color:rgba(255,255,255,0.08);" +
               "-fx-border-radius:12;" +
               "-fx-background-radius:12;" +
               "-fx-padding:9 12 9 12;" +
               "-fx-font-size:12px;" +
               "-fx-font-family:" + BODY_FONT + ";";
    }

    private String comboStyle() {
        return "-fx-background-color:rgba(8,17,27,0.85);" +
               "-fx-text-fill:" + T_IVORY + ";" +
               "-fx-border-color:rgba(255,255,255,0.08);" +
               "-fx-border-radius:12;" +
               "-fx-background-radius:12;" +
               "-fx-font-size:12px;" +
               "-fx-font-family:" + BODY_FONT + ";";
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(bodyMutedStyle(11));
        return label;
    }

    private Label overline(String text, String color) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill:" + color + ";" +
            "-fx-font-size:10px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-font-weight:700;" +
            "-fx-letter-spacing:1.6px;"
        );
        return label;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:28px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;"
        );
        return label;
    }

    private Label bigMetric(String value, String color) {
        Label label = new Label(value);
        label.setStyle(
            "-fx-text-fill:" + color + ";" +
            "-fx-font-size:32px;" +
            "-fx-font-family:" + DISPLAY_FONT + ";" +
            "-fx-font-weight:bold;"
        );
        return label;
    }

    private Label labelText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(bodyMutedStyle(12));
        return label;
    }

    private Label bodyLabel(String text, int size, String color, boolean strong) {
        Label label = new Label(text);
        label.setStyle(bodyStyle(size, color, strong));
        return label;
    }

    private String bodyStyle(int size, String color, boolean strong) {
        return "-fx-text-fill:" + color + ";" +
               "-fx-font-size:" + size + "px;" +
               "-fx-font-family:" + BODY_FONT + ";" +
               "-fx-font-weight:" + (strong ? "600" : "400") + ";";
    }

    private String bodyMutedStyle(int size) {
        return bodyStyle(size, T_MUTED, false);
    }

    private VBox heroValue(String label, String value, String tone) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
            "-fx-background-color:rgba(255,255,255,0.03);" +
            "-fx-background-radius:16;" +
            "-fx-border-color:rgba(255,255,255,0.05);" +
            "-fx-border-radius:16;"
        );
        box.getChildren().addAll(overline(label.toUpperCase(), T_SUBTLE), bigMetric(value, tone));
        return box;
    }

    private HBox operationLine(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label left = new Label(label);
        left.setMinWidth(140);
        left.setStyle(bodyStyle(12, T_SUBTLE, true));
        Label right = new Label(value);
        right.setWrapText(true);
        right.setStyle(bodyStyle(13, T_IVORY, false));
        row.getChildren().addAll(left, right);
        return row;
    }

    private VBox slimInfoCard(String title, String value, String accent) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.03);" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + accent + "22;" +
            "-fx-border-radius:14;"
        );
        card.getChildren().addAll(overline(title.toUpperCase(), accent), bodyLabel(value, 13, T_IVORY, false));
        return card;
    }

    private Node divider() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setStyle("-fx-background-color:rgba(255,255,255,0.08);");
        return line;
    }

    private Label emptyState(String text) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill:" + T_SUBTLE + ";" +
            "-fx-font-size:12px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-padding:18 0 8 0;"
        );
        return label;
    }

    private Label stateChip(String text, String accent) {
        Label chip = new Label(text);
        chip.setStyle(
            "-fx-background-color:" + rgba(accent, 0.12) + ";" +
            "-fx-border-color:" + rgba(accent, 0.32) + ";" +
            "-fx-border-radius:999;" +
            "-fx-background-radius:999;" +
            "-fx-padding:5 11 5 11;" +
            "-fx-text-fill:" + accent + ";" +
            "-fx-font-size:11px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-font-weight:700;"
        );
        return chip;
    }

    private Label runwayCode(String text) {
        Label chip = new Label(text);
        chip.setStyle(
            "-fx-background-color:rgba(255,255,255,0.05);" +
            "-fx-background-radius:10;" +
            "-fx-padding:5 10 5 10;" +
            "-fx-text-fill:" + T_IVORY + ";" +
            "-fx-font-size:11px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-font-weight:600;"
        );
        return chip;
    }

    private Label boardLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setStyle(
            "-fx-text-fill:" + T_SUBTLE + ";" +
            "-fx-font-size:10px;" +
            "-fx-font-family:" + BODY_FONT + ";" +
            "-fx-font-weight:700;" +
            "-fx-letter-spacing:1.2px;"
        );
        return label;
    }

    private Node boardCell(String text, double width, String color, boolean strong) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setWrapText(true);
        label.setStyle(bodyStyle(12, color, strong));
        return label;
    }

    private Node fixedNode(Node node, double width) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        return box;
    }

    private <S> TableColumn<S, String> tableColumn(String name, double width) {
        TableColumn<S, String> column = new TableColumn<>(name);
        column.setPrefWidth(width);
        return column;
    }

    private HBox specLine(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label left = new Label(label);
        left.setMinWidth(86);
        left.setStyle(bodyStyle(11, T_SUBTLE, true));
        Label right = new Label(value);
        right.setWrapText(true);
        right.setStyle(bodyStyle(12, T_IVORY, false));
        row.getChildren().addAll(left, right);
        return row;
    }

    private <S> void chipCell(TableColumn<S, String> column, String match, String accent, String neutral) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                String tone = value.contains(match) ? accent : neutral;
                setText(value);
                setStyle(
                    "-fx-background-color:rgba(19,32,49,0.92);" +
                    "-fx-text-fill:" + tone + ";" +
                    "-fx-font-size:11px;" +
                    "-fx-font-family:" + BODY_FONT + ";" +
                    "-fx-font-weight:600;" +
                    "-fx-padding:6 10 6 10;"
                );
            }
        });
    }

    private String tableStyle() {
        return "-fx-background-color:rgba(19,32,49,0.96);" +
               "-fx-control-inner-background:rgba(19,32,49,0.96);" +
               "-fx-background-radius:16;" +
               "-fx-border-radius:16;" +
               "-fx-border-color:rgba(255,255,255,0.06);" +
               "-fx-table-cell-border-color:rgba(255,255,255,0.04);" +
               "-fx-padding:8;" +
               "-fx-font-size:12px;" +
               "-fx-font-family:" + BODY_FONT + ";" +
               "-fx-text-fill:" + T_IVORY + ";";
    }

    private String panelStyle(String accent, boolean compact) {
        return "-fx-background-color:linear-gradient(to bottom, rgba(19,32,49,0.98), rgba(23,38,56,0.96));" +
               "-fx-background-radius:" + (compact ? "18" : "22") + ";" +
               "-fx-border-color:" + rgba(accent, 0.20) + ";" +
               "-fx-border-radius:" + (compact ? "18" : "22") + ";" +
               "-fx-border-width:1;" +
               "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.20), 18, 0, 0, 6);";
    }

    private String appShellStyle() {
        return "-fx-background-color:linear-gradient(to bottom, " + T_BG + ", " + T_CHARCOAL + ");";
    }

    private Button navButton(String code, String label) {
        Button button = new Button(code + "\n" + label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(58);
        button.setWrapText(true);
        button.setStyle(navButtonStyle(false, false));
        button.setOnMouseEntered(e -> {
            if (button != activeBtn) button.setStyle(navButtonStyle(false, true));
        });
        button.setOnMouseExited(e -> {
            if (button != activeBtn) button.setStyle(navButtonStyle(false, false));
        });
        return button;
    }

    private Button quickAction(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(ghostButtonStyle(T_BRASS, true));
        button.setOnMouseEntered(e -> button.setStyle(ghostButtonStyle(T_BRASS, false)));
        button.setOnMouseExited(e -> button.setStyle(ghostButtonStyle(T_BRASS, true)));
        button.setOnAction(e -> action.run());
        return button;
    }

    private Button primaryButton(String text, String accent) {
        Button button = new Button(text);
        button.setStyle(primaryButtonStyle(accent, false));
        button.setOnMouseEntered(e -> button.setStyle(primaryButtonStyle(accent, true)));
        button.setOnMouseExited(e -> button.setStyle(primaryButtonStyle(accent, false)));
        return button;
    }

    private Button secondaryButton(String text, String accent) {
        Button button = new Button(text);
        button.setStyle(ghostButtonStyle(accent, true));
        button.setOnMouseEntered(e -> button.setStyle(ghostButtonStyle(accent, false)));
        button.setOnMouseExited(e -> button.setStyle(ghostButtonStyle(accent, true)));
        return button;
    }

    private String navButtonStyle(boolean active, boolean hover) {
        String bg = active ? "rgba(111,192,188,0.14)" : hover ? "rgba(255,255,255,0.05)" : "transparent";
        String border = active ? rgba(T_TEAL, 0.45) : hover ? "rgba(255,255,255,0.06)" : "transparent";
        String text = active ? T_IVORY : hover ? T_IVORY : T_MUTED;
        return "-fx-background-color:" + bg + ";" +
               "-fx-border-color:" + border + ";" +
               "-fx-border-radius:16;" +
               "-fx-background-radius:16;" +
               "-fx-border-width:1;" +
               "-fx-text-fill:" + text + ";" +
               "-fx-font-size:11px;" +
               "-fx-font-family:" + BODY_FONT + ";" +
               "-fx-font-weight:" + (active ? "700" : "600") + ";" +
               "-fx-line-spacing:2px;" +
               "-fx-alignment:center;" +
               "-fx-cursor:hand;";
    }

    private String ghostButtonStyle(String accent, boolean quiet) {
        return "-fx-background-color:" + (quiet ? "transparent" : rgba(accent, 0.10)) + ";" +
               "-fx-border-color:" + rgba(accent, quiet ? 0.22 : 0.36) + ";" +
               "-fx-border-radius:999;" +
               "-fx-background-radius:999;" +
               "-fx-text-fill:" + accent + ";" +
               "-fx-font-size:11px;" +
               "-fx-font-family:" + BODY_FONT + ";" +
               "-fx-font-weight:700;" +
               "-fx-cursor:hand;" +
               "-fx-padding:8 14 8 14;";
    }

    private String primaryButtonStyle(String accent, boolean hover) {
        return "-fx-background-color:" + (hover ? lighten(accent, 0.10) : accent) + ";" +
               "-fx-background-radius:999;" +
               "-fx-text-fill:" + T_CHARCOAL + ";" +
               "-fx-font-size:12px;" +
               "-fx-font-family:" + BODY_FONT + ";" +
               "-fx-font-weight:700;" +
               "-fx-cursor:hand;" +
               "-fx-padding:9 16 9 16;";
    }

    private HBox buildLiveBadge() {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(8, 14, 8, 14));
        badge.setStyle(
            "-fx-background-color:rgba(111,192,188,0.08);" +
            "-fx-background-radius:999;" +
            "-fx-border-color:rgba(111,192,188,0.18);" +
            "-fx-border-radius:999;"
        );

        Circle dot = new Circle(4, Color.web(T_TEAL));
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.8), dot);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.35);
        pulse.setToY(1.35);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Label text = new Label("System operational");
        text.setStyle(bodyStyle(12, T_TEAL, true));
        badge.getChildren().addAll(dot, text);
        return badge;
    }

    private Region sectionSpacer() {
        Region region = new Region();
        region.setPrefHeight(8);
        return region;
    }

    private void updateClock() {
        clockLbl.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  •  dd/MM/yyyy")));
    }

    private void flashNotice(String text, String tone) {
        pageNoticeText = text;
        pageNoticeTone = tone;
    }

    private void openSection(Button button, Runnable action) {
        setActive(button);
        action.run();
    }

    private void setActive(Button button) {
        if (activeBtn != null) {
            activeBtn.setStyle(navButtonStyle(false, false));
        }
        activeBtn = button;
        activeBtn.setStyle(navButtonStyle(true, false));
    }

    private ScrollPane scrollOf(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background:transparent;" +
            "-fx-background-color:transparent;" +
            "-fx-border-color:transparent;"
        );
        return scroll;
    }

    private void swap(Node node) {
        node.setOpacity(0);
        node.setTranslateY(12);

        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        Timeline rise = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), 12)),
            new KeyFrame(Duration.millis(220), new KeyValue(node.translateYProperty(), 0, Interpolator.EASE_OUT))
        );

        ParallelTransition transition = new ParallelTransition(fade, rise);
        transition.play();

        mainContent.getChildren().setAll(node);
    }

    private void animateNavRailIn() {
        navRail.setTranslateX(-28);
        navRail.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(280), navRail);
        fade.setFromValue(0);
        fade.setToValue(1);
        Timeline move = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(navRail.translateXProperty(), -28)),
            new KeyFrame(Duration.millis(280), new KeyValue(navRail.translateXProperty(), 0, Interpolator.EASE_OUT))
        );
        new ParallelTransition(fade, move).play();
    }

    private int activeFlightsCount() {
        return (int) aeroport.getVols().values().stream()
            .filter(v -> v.getStatut() == StatutVol.EMBARQUEMENT || v.getStatut() == StatutVol.EN_VOL || v.getStatut() == StatutVol.RETARDE)
            .count();
    }

    private int countFlights(StatutVol statut) {
        return (int) aeroport.getVols().values().stream().filter(v -> v.getStatut() == statut).count();
    }

    private String spotlightFlightText() {
        return aeroport.getVols().values().stream()
            .filter(v -> v.getStatut() == StatutVol.EMBARQUEMENT || v.getStatut() == StatutVol.EN_VOL)
            .findFirst()
            .map(v -> v.getNumeroVol() + " · " + v.getVilleDepart() + " to " + v.getVilleArrivee())
            .orElse("No flight currently in a live movement state.");
    }

    private String occupiedRunwayText() {
        return aeroport.getPistes().stream()
            .filter(p -> !p.isDisponible() && p.getVolEnCours() != null)
            .findFirst()
            .map(p -> p.getNumeroPiste() + " assigned to " + p.getVolEnCours().getNumeroVol())
            .orElse("All runways currently open for assignment.");
    }

    private String longHaulReadinessText() {
        long qualifiedPilots = aeroport.getPilotes().stream().filter(Pilote::estQualifieLongCourrier).count();
        long capableFleet = aeroport.getFlotte().stream().filter(a -> a.peutEffectuerVol(5000)).count();
        return qualifiedPilots + " long-haul pilots aligned with " + capableFleet + " aircraft capable beyond 5000 km.";
    }

    private String priorityPassengerText() {
        long priority = aeroport.getPassagers().values().stream().filter(Passager::isPassagerPrioritaire).count();
        return priority + " priority-service guests currently registered in the system.";
    }

    private String maintenanceWatchText() {
        long inMaintenance = aeroport.getFlotte().stream().filter(Avion::isEnMaintenance).count();
        return inMaintenance == 0 ? "No aircraft currently in maintenance." : inMaintenance + " aircraft currently in maintenance watch.";
    }

    private String crewPostureText() {
        long qualified = aeroport.getPilotes().stream().filter(Pilote::estQualifieLongCourrier).count();
        return qualified + " pilots are cleared for long-haul service.";
    }

    private List<String> fleetSummaryLines() {
        List<String> lines = new ArrayList<>();
        long ready = aeroport.getFlotte().stream().filter(a -> !a.isEnMaintenance()).count();
        long maintenance = aeroport.getFlotte().size() - ready;
        lines.add(ready + " aircraft ready for dispatch.");
        lines.add(maintenance + " aircraft are in maintenance posture.");
        lines.add(aeroport.getFlotte().stream().filter(a -> a.peutEffectuerVol(5000)).count() + " units can cover long-haul sectors.");
        return lines;
    }

    private List<String> runwaySummaryLines() {
        List<String> lines = new ArrayList<>();
        long available = aeroport.getPistes().stream().filter(Piste::isDisponible).count();
        lines.add(available + " runways are currently open.");
        lines.add((aeroport.getPistes().size() - available) + " runways are allocated.");
        aeroport.getPistes().stream()
            .filter(p -> !p.isDisponible() && p.getVolEnCours() != null)
            .findFirst()
            .ifPresent(p -> lines.add("Primary occupied strip: " + p.getNumeroPiste() + " with " + p.getVolEnCours().getNumeroVol() + "."));
        if (lines.size() < 3) {
            lines.add("No runway conflicts detected in the current roster.");
        }
        return lines;
    }

    private List<String> personnelSummaryLines() {
        List<String> lines = new ArrayList<>();
        lines.add(aeroport.getPilotes().size() + " pilots in the aircrew registry.");
        lines.add(aeroport.getAgentsSol().size() + " ground agents supporting operations.");
        lines.add(aeroport.getPassagers().size() + " passengers tracked across services.");
        return lines;
    }

    private String assignedRunway(Vol vol) {
        return aeroport.getPistes().stream()
            .filter(p -> p.getVolEnCours() == vol)
            .map(Piste::getNumeroPiste)
            .findFirst()
            .orElse("Open");
    }

    private String statusLabel(StatutVol statut) {
        return switch (statut) {
            case PROGRAMME -> "Scheduled";
            case EMBARQUEMENT -> "Boarding";
            case EN_VOL -> "Airborne";
            case ARRIVE -> "Arrived";
            case ANNULE -> "Cancelled";
            case RETARDE -> "Delayed";
        };
    }

    private String statusTone(StatutVol statut) {
        return switch (statut) {
            case PROGRAMME -> T_BRASS;
            case EMBARQUEMENT -> T_TEAL;
            case EN_VOL -> T_BLUE;
            case ARRIVE -> T_SAGE;
            case ANNULE -> T_RED;
            case RETARDE -> T_BRASS;
        };
    }

    private String loadLabel(Vol vol) {
        int total = vol.getAvion().getCapacitePassagers();
        int occupied = total - vol.placesDisponibles();
        return occupied + "/" + total;
    }

    private String loadTone(Vol vol) {
        int total = vol.getAvion().getCapacitePassagers();
        int occupied = total - vol.placesDisponibles();
        double ratio = total == 0 ? 0 : (double) occupied / total;
        if (ratio >= 0.85) return T_RED;
        if (ratio >= 0.60) return T_BRASS;
        return T_SAGE;
    }

    private String rgba(String hex, double alpha) {
        Color color = Color.web(hex);
        return String.format(
            "rgba(%d,%d,%d,%.3f)",
            (int) Math.round(color.getRed() * 255),
            (int) Math.round(color.getGreen() * 255),
            (int) Math.round(color.getBlue() * 255),
            alpha
        );
    }

    private String lighten(String hex, double factor) {
        Color color = Color.web(hex);
        double r = Math.min(1.0, color.getRed() + (1.0 - color.getRed()) * factor);
        double g = Math.min(1.0, color.getGreen() + (1.0 - color.getGreen()) * factor);
        double b = Math.min(1.0, color.getBlue() + (1.0 - color.getBlue()) * factor);
        return String.format("#%02X%02X%02X", (int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255));
    }

    private void initDemoData() {
        aeroport = new GestionAeroport("Aéroport International de Tunis-Carthage");

        aeroport.ajouterAvion(new Avion("TU-JAL", "Airbus A320", 180, 6100.0));
        aeroport.ajouterAvion(new Avion("TU-BOE", "Boeing 737", 160, 5765.0));
        aeroport.ajouterAvion(new Avion("TU-LNG", "Airbus A350", 350, 15000.0));
        Avion a4 = new Avion("TU-REG", "ATR 72", 72, 1500.0);
        aeroport.ajouterAvion(a4);
        a4.mettreEnMaintenance();

        aeroport.ajouterPilote(new Pilote("PIL001", "Ben Ali", "Karim", "k.benali@airport.tn", "LIC-001", 3500, "pass123"));
        aeroport.ajouterPilote(new Pilote("PIL002", "Meddeb", "Sonia", "s.meddeb@airport.tn", "LIC-002", 800, "pass456"));
        aeroport.ajouterPilote(new Pilote("PIL003", "Trabelsi", "Ahmed", "a.trabelsi@airport.tn", "LIC-003", 1200, "pass789"));

        aeroport.ajouterPassager(new Passager("PAS001", "Gharbi", "Leila", "l.gharbi@gmail.com", "TN123456", "Tunisienne"));
        aeroport.ajouterPassager(new Passager("PAS002", "Slama", "Mohamed", "m.slama@gmail.com", "TN789012", "Tunisien"));
        aeroport.ajouterPassager(new Passager("PAS003", "Dupont", "Claire", "c.dupont@gmail.fr", "FR345678", "Française", true));
        aeroport.ajouterPassager(new Passager("PAS004", "Hassan", "Omar", "o.hassan@gmail.com", "DZ567890", "Algérien"));
        aeroport.ajouterPassager(new Passager("PAS005", "Martin", "Sophie", "s.martin@gmail.fr", "FR901234", "Française"));

        aeroport.ajouterAgentSol(new AgentSol("AGS001", "Ben Salem", "Nour", "n.bensalem@airport.tn", "Enregistrement", 5));
        aeroport.ajouterAgentSol(new AgentSol("AGS002", "Jaziri", "Walid", "w.jaziri@airport.tn", "Bagages", 4));
        aeroport.ajouterAgentSol(new AgentSol("AGS003", "Kefi", "Amira", "a.kefi@airport.tn", "Sécurité", 2));

        aeroport.ajouterPiste(new Piste("P01", 3200.0));
        aeroport.ajouterPiste(new Piste("P02", 2800.0));
        aeroport.ajouterPiste(new Piste("P03", 1500.0));

        try {
            aeroport.creerVol("TU101", "Tunis", "Paris", 1700.0, "TU-JAL", "PIL001");
            aeroport.creerVol("TU202", "Tunis", "Montréal", 7500.0, "TU-LNG", "PIL001");
            aeroport.creerVol("TU303", "Tunis", "Djerba", 400.0, "TU-BOE", "PIL003");
        } catch (Exception ignored) {
        }

        aeroport.reserverPassager("PAS001", "TU101");
        aeroport.reserverPassager("PAS002", "TU101");
        aeroport.reserverPassager("PAS003", "TU202");

        Vol tu101 = aeroport.getVols().get("TU101");
        if (tu101 != null) {
            tu101.changerStatut(StatutVol.EMBARQUEMENT);
            aeroport.getPistes().get(0).assignerVol(tu101);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
