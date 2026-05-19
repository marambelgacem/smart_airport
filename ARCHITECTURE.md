# Smart Airport Architecture

## Overview

Smart Airport follows a simple layered structure:

- bootstrap layer: starts the modular JavaFX application
- domain model layer: aircraft, people, runways, flights, statuses
- service layer: `GestionAeroport` orchestrates business rules and collections
- UI layer: `AirportApp` builds screens, forms, tables, and dashboards
- test layer: smoke tests validate the main workflows

## Runtime Flow

1. `MainLauncher` delegates startup to `AirportApp`.
2. `AirportApp.start()` calls `initDemoData()` to seed the in-memory airport.
3. The UI renders the dashboard and navigation.
4. Form actions call `GestionAeroport` or directly update domain objects.
5. The UI refreshes sections after each operation.

## Package Responsibilities

| Package | Responsibility |
|---|---|
| `airport` | bootstrap entrypoint |
| `airport.collection` | airport-level coordination and collection management |
| `airport.exception` | business exceptions |
| `airport.model.avion` | aircraft model |
| `airport.model.personnel` | people hierarchy and personnel contracts |
| `airport.model.service` | runway model |
| `airport.model.vol` | flight model and status enum |
| `airport.ui` | JavaFX application and all screens |
| `src/test/java` | workflow smoke tests |

## Dependency Direction

```text
MainLauncher
    -> AirportApp
        -> GestionAeroport
            -> Avion / Pilote / Passager / AgentSol / Piste / Vol / StatutVol
            -> custom exceptions

AirportApp
    -> JavaFX controls/layouts
    -> GestionAeroport
    -> domain objects for display and direct state updates
```

## File and Class Reference

### `src/module-info.java`

Declares the `airport` Java module, exports the domain packages, and opens `airport.ui` to JavaFX so the application can launch correctly.

### `airport.MainLauncher`

Purpose: minimal bootstrap entrypoint.

| Method | Brief role |
|---|---|
| `MainLauncher()` | private constructor to prevent instantiation |
| `main(String[] args)` | forwards startup to `AirportApp.main()` |

### `airport.collection.GestionAeroport`

Purpose: central service/facade for in-memory airport operations.

Internal collections:

- `Map<String, Vol>` stores flights by flight number
- `Map<String, Passager>` stores passengers by id
- `List<Avion>` stores fleet order
- `List<Piste>` stores runway order
- `List<AgentSol>` stores ground staff
- `Set<Pilote>` stores unique pilots

| Method | Brief role |
|---|---|
| `GestionAeroport(String nomAeroport)` | creates an airport service instance |
| `ajouterAvion(Avion avion)` | adds an aircraft to the fleet |
| `trouverAvion(String immat)` | finds an aircraft by registration |
| `ajouterPilote(Pilote p)` | adds a pilot to the pilot set |
| `trouverPilote(String id)` | finds a pilot by id |
| `ajouterPassager(Passager p)` | registers a passenger in the passenger map |
| `trouverPassager(String id)` | returns a passenger by id |
| `ajouterAgentSol(AgentSol agent)` | adds a ground agent |
| `ajouterPiste(Piste p)` | adds a runway |
| `trouverPisteDisponible()` | returns the first available runway |
| `creerVol(...)` | validates aircraft and pilot constraints, then creates and stores a flight |
| `reserverPassager(String idPassager, String numeroVol)` | books a passenger on a flight and returns a user-facing result message |
| `getToutLePersonnel()` | merges pilots, agents, and passengers into one polymorphic list |
| `getNomAeroport()` | returns airport name |
| `getVols()` | exposes the flights map |
| `getFlotte()` | exposes the fleet list |
| `getPilotes()` | exposes the pilots set |
| `getPassagers()` | exposes the passenger map |
| `getPistes()` | exposes the runway list |
| `getAgentsSol()` | exposes the ground-agent list |

### `airport.exception.AvionEnMaintenanceException`

Purpose: signals that a selected aircraft cannot be used because it is in maintenance.

| Method | Brief role |
|---|---|
| `AvionEnMaintenanceException(String immat)` | builds a readable message from the aircraft registration |

### `airport.exception.PiloteNonQualifieException`

Purpose: signals that a pilot is not qualified for the requested flight.

| Method | Brief role |
|---|---|
| `PiloteNonQualifieException(String idPilote, String raison)` | builds the qualification error message |
| `getIdPilote()` | returns the blocked pilot id |

### `airport.exception.VolCompletException`

Purpose: signals that a flight has no remaining seats.

| Method | Brief role |
|---|---|
| `VolCompletException(String message)` | wraps the seat-capacity failure message |

### `airport.exception.VolIndisponibleException`

Purpose: signals that a flight cannot accept passenger changes because of its status.

| Method | Brief role |
|---|---|
| `VolIndisponibleException(String message)` | wraps the availability failure message |

### `airport.exception.VolInexistantException`

Purpose: generic missing-flight exception type.

| Method | Brief role |
|---|---|
| `VolInexistantException(String message)` | wraps the missing-flight message |

### `airport.model.avion.Avion`

Purpose: aircraft entity with capacity, range, and maintenance state.

| Method | Brief role |
|---|---|
| `Avion(String immatriculation, String modele, int capacitePassagers, double autonomieKm)` | creates an aircraft |
| `peutEffectuerVol(double distanceKm)` | checks both range and maintenance readiness |
| `mettreEnMaintenance()` | sets maintenance state to true |
| `sortirDeMaintenance()` | clears maintenance state |
| `getImmatriculation()` | returns registration |
| `getModele()` | returns model |
| `getCapacitePassagers()` | returns seat capacity |
| `getAutonomieKm()` | returns range in kilometers |
| `isEnMaintenance()` | returns maintenance state |
| `toString()` | returns a readable aircraft summary |

### `airport.model.personnel.Authentifiable`

Purpose: contract for login-capable personnel.

| Method | Brief role |
|---|---|
| `seConnecter(String motDePasse)` | attempts authentication |
| `seDeconnecter()` | logs out |
| `estConnecte()` | returns current authentication state |

### `airport.model.personnel.Notifiable`

Purpose: contract for personnel who can receive notifications.

| Method | Brief role |
|---|---|
| `recevoirNotification(String message)` | sends a default-priority notification |
| `recevoirNotification(String message, int priorite)` | sends a notification with explicit priority |

### `airport.model.personnel.Personne`

Purpose: abstract base class for all people in the airport domain.

| Method | Brief role |
|---|---|
| `Personne(String id, String nom, String prenom, String email)` | initializes the shared identity fields |
| `getRole()` | abstract role label implemented by subclasses |
| `afficherInfos()` | prints a short console summary |
| `getId()` | returns id |
| `getNom()` | returns last name |
| `getPrenom()` | returns first name |
| `getEmail()` | returns email |
| `setEmail(String email)` | updates email |
| `equals(Object o)` | compares people by id |
| `hashCode()` | hashes by id |
| `toString()` | returns a readable polymorphic label |

### `airport.model.personnel.Pilote`

Purpose: pilot entity with authentication, notifications, and qualification rules.

| Method | Brief role |
|---|---|
| `Pilote(...)` | creates a pilot |
| `getRole()` | returns `"Pilote"` |
| `seConnecter(String motDePasse)` | authenticates the pilot |
| `seDeconnecter()` | logs out the pilot |
| `estConnecte()` | reports whether the pilot is logged in |
| `recevoirNotification(String message)` | sends a normal-priority notification |
| `recevoirNotification(String message, int priorite)` | stores a notification with formatted priority prefix |
| `estQualifieLongCourrier()` | checks long-haul eligibility from flight hours |
| `ajouterHeuresVol(int h)` | increments flight hours when positive |
| `getLicencePilote()` | returns license id |
| `getHeuresDeVol()` | returns accumulated flight hours |
| `getNotifications()` | returns stored notifications |

### `airport.model.personnel.Passager`

Purpose: passenger entity with passport, nationality, and priority status.

| Method | Brief role |
|---|---|
| `Passager(String id, String nom, String prenom, String email, String numeroPasport, String nationalite)` | creates a regular passenger |
| `Passager(..., boolean prioritaire)` | creates a passenger and sets priority state |
| `getRole()` | returns passenger role label based on priority |
| `getNumeroPasport()` | returns passport number |
| `getNationalite()` | returns nationality |
| `isPassagerPrioritaire()` | returns priority flag |
| `setPassagerPrioritaire(boolean p)` | updates priority flag |

### `airport.model.personnel.AgentSol`

Purpose: ground-staff entity with sector, experience, and notifications.

| Method | Brief role |
|---|---|
| `AgentSol(...)` | creates a ground agent |
| `getRole()` | returns a role label with sector |
| `recevoirNotification(String message)` | stores a default notification |
| `recevoirNotification(String message, int priorite)` | stores a priority-tagged notification |
| `peutSuperviser()` | checks whether experience is high enough for supervision |
| `getSecteur()` | returns sector |
| `getExperience()` | returns experience level |

### `airport.model.service.Piste`

Purpose: runway entity that can be free or assigned to a flight.

| Method | Brief role |
|---|---|
| `Piste(String numeroPiste, double longueurMetres)` | creates a runway |
| `assignerVol(Vol vol)` | assigns a flight if the runway is free |
| `libererPiste()` | clears the current assignment |
| `getNumeroPiste()` | returns runway code |
| `getLongueurMetres()` | returns length |
| `isDisponible()` | returns availability state |
| `getVolEnCours()` | returns the currently assigned flight |
| `toString()` | returns a readable runway summary |

### `airport.model.vol.StatutVol`

Purpose: enum of all supported flight states.

States:

- `PROGRAMME`
- `EMBARQUEMENT`
- `EN_VOL`
- `ARRIVE`
- `ANNULE`
- `RETARDE`

| Method | Brief role |
|---|---|
| `getLibelle()` | returns the French label |
| `getEmoji()` | returns the display emoji |
| `toString()` | returns a combined emoji + label string |

### `airport.model.vol.Vol`

Purpose: flight entity linking route, aircraft, pilot, status, and passengers.

| Method | Brief role |
|---|---|
| `Vol(...)` | creates a flight and starts it in `PROGRAMME` state |
| `ajouterPassager(Passager passager)` | adds a passenger if the flight is open and not full |
| `retirerPassager(String idPassager)` | removes a passenger by id |
| `changerStatut(StatutVol s)` | updates the flight status |
| `afficherInfos()` | prints a compact console view |
| `afficherInfos(boolean detail)` | prints flight info and optionally all passengers |
| `placesDisponibles()` | computes remaining seats |
| `getNumeroVol()` | returns flight number |
| `getVilleDepart()` | returns departure city |
| `getVilleArrivee()` | returns arrival city |
| `getDistanceKm()` | returns flight distance |
| `getStatut()` | returns status |
| `getAvion()` | returns aircraft |
| `getPilote()` | returns pilot |
| `getPassagers()` | returns passenger list |
| `toString()` | returns a readable flight summary |

### `airport.ui.AirportApp`

Purpose: the full JavaFX application, including dashboard, forms, tables, navigation, styling, summaries, and demo-data bootstrapping.

Nested records:

| Nested type | Brief role |
|---|---|
| `SearchOption` | value/label pair for selector entries |
| `SearchSelector` | wrapper holding the search field, combo box, and source options |

#### Lifecycle and navigation methods

| Method | Brief role |
|---|---|
| `start(Stage stage)` | initializes demo data, builds the shell, and shows the app |
| `buildTopBar()` | creates the top header with branding, quick actions, live badge, and clock |
| `buildNavRail()` | creates the left navigation rail and exit button |
| `showDashboard()` | renders the overview dashboard |
| `showVols()` | renders the flights page |
| `showAvions()` | renders the fleet page |
| `showPilotes()` | renders the pilots page |
| `showPassagers()` | renders the passengers page |
| `showPistes()` | renders the runways page |
| `showPersonnel()` | renders the combined personnel page |
| `openSection(Button button, Runnable action)` | switches the active section |
| `setActive(Button button)` | updates nav-button visual state |
| `swap(Node node)` | replaces the center content with animation |
| `animateNavRailIn()` | animates the nav rail on startup |
| `main(String[] args)` | launches the JavaFX app |

#### Page content builders

| Method | Brief role |
|---|---|
| `buildLiveOperationsPanel()` | builds the main live dashboard hero panel |
| `buildFlightBoardPanel(...)` | builds the flight board card |
| `buildSummaryPanel(...)` | builds a text-summary panel |
| `buildCreateFlightPanel()` | builds the flight-creation form |
| `buildReservationPanel()` | builds the passenger reservation form |
| `buildStatusPanel()` | builds the flight-status update form |
| `buildAddAircraftPanel()` | builds the aircraft creation form |
| `buildMaintenancePanel()` | builds the maintenance control form |
| `buildAddPilotPanel()` | builds the pilot creation form |
| `buildAddPassengerPanel()` | builds the passenger creation form |
| `buildAddRunwayPanel()` | builds the runway creation form |
| `buildRunwayActionPanel()` | builds the runway assignment/release form |
| `buildPilotsTable()` | builds the pilots table view |
| `buildPassengersTable()` | builds the passengers table view |
| `buildPersonnelTable()` | builds the combined personnel table |
| `buildAircraftCard(Avion avion)` | builds a visual aircraft status card |
| `buildRunwayStrip(Piste piste)` | builds a visual runway strip card |
| `boardHeader(boolean compact)` | builds the flight-board header row |
| `boardRow(Vol vol, boolean compact)` | builds one flight row in the board |
| `buildMetricRow(VBox... cards)` | lays out metric cards in a row |
| `metricCard(...)` | builds a single metric card |
| `premiumPanel(...)` | wraps content in the shared premium panel container |
| `page()` | creates a standard page container |
| `pageTitle(String title, String caption)` | builds a page title block |
| `appendNotice(VBox page)` | inserts the transient page notice when present |

#### Form and search helpers

| Method | Brief role |
|---|---|
| `grid()` | creates a standard form grid |
| `field(String prompt)` | creates a styled text field |
| `searchableSelector(...)` | creates a search field + combo selector pair |
| `selectedValue(SearchSelector selector)` | resolves the selected or typed backing value |
| `fieldLabel(String text)` | creates a small form label |

#### Typography and small component helpers

| Method | Brief role |
|---|---|
| `overline(String text, String color)` | creates a small uppercase label |
| `sectionTitle(String text)` | creates a section heading |
| `bigMetric(String value, String color)` | creates a large highlighted metric |
| `labelText(String text)` | creates a muted label |
| `bodyLabel(String text, int size, String color, boolean strong)` | creates configurable body text |
| `bodyStyle(int size, String color, boolean strong)` | returns shared body-text CSS |
| `bodyMutedStyle(int size)` | returns muted body-text CSS |
| `heroValue(String label, String value, String tone)` | builds a hero metric block |
| `operationLine(String label, String value)` | builds a summary line |
| `slimInfoCard(String title, String value, String accent)` | builds a compact info card |
| `divider()` | creates a horizontal divider |
| `emptyState(String text)` | creates an empty-state label |
| `stateChip(String text, String accent)` | creates a colored status chip |
| `runwayCode(String text)` | creates a runway badge |
| `boardLabel(String text, double width)` | creates a board header label |
| `boardCell(String text, double width, String color, boolean strong)` | creates a board cell |
| `fixedNode(Node node, double width)` | fixes node width in layouts |
| `tableColumn(String name, double width)` | creates a standard table column |
| `specLine(String label, String value)` | creates a label/value specification line |
| `chipCell(...)` | customizes table cells as status chips |

#### Styling helpers

| Method | Brief role |
|---|---|
| `fieldStyle()` | returns text-field CSS |
| `comboStyle()` | returns combo-box CSS |
| `tableStyle()` | returns table CSS |
| `panelStyle(String accent, boolean compact)` | returns panel CSS |
| `appShellStyle()` | returns root-shell CSS |
| `navButtonStyle(boolean active, boolean hover)` | returns nav-button CSS |
| `ghostButtonStyle(String accent, boolean quiet)` | returns secondary-button CSS |
| `primaryButtonStyle(String accent, boolean hover)` | returns primary-button CSS |
| `rgba(String hex, double alpha)` | converts a hex color to rgba |
| `lighten(String hex, double factor)` | lightens a hex color |

#### Button and chrome helpers

| Method | Brief role |
|---|---|
| `navButton(String code, String label)` | builds a navigation button |
| `quickAction(String text, Runnable action)` | builds a top-bar quick-action button |
| `primaryButton(String text, String accent)` | builds a primary action button |
| `secondaryButton(String text, String accent)` | builds a secondary action button |
| `buildLiveBadge()` | builds the live-status badge |
| `sectionSpacer()` | creates vertical spacing in the nav rail |
| `updateClock()` | refreshes the local-time label |
| `flashNotice(String text, String tone)` | stores a notice and refreshes the dashboard |
| `scrollOf(VBox content)` | wraps page content in a scroll pane |

#### Dashboard summary helpers

| Method | Brief role |
|---|---|
| `activeFlightsCount()` | counts active-flight states |
| `countFlights(StatutVol statut)` | counts flights by one status |
| `spotlightFlightText()` | creates the dashboard spotlight sentence |
| `occupiedRunwayText()` | summarizes occupied runway count |
| `longHaulReadinessText()` | summarizes long-haul-capable aircraft |
| `priorityPassengerText()` | summarizes priority passengers |
| `maintenanceWatchText()` | summarizes maintenance count |
| `crewPostureText()` | summarizes crew posture |
| `fleetSummaryLines()` | builds dashboard fleet summary lines |
| `runwaySummaryLines()` | builds dashboard runway summary lines |
| `personnelSummaryLines()` | builds dashboard personnel summary lines |
| `assignedRunway(Vol vol)` | returns the runway code assigned to a flight |
| `statusLabel(StatutVol statut)` | maps a status to display text |
| `statusTone(StatutVol statut)` | maps a status to a display color |
| `loadLabel(Vol vol)` | maps flight load to a readable label |
| `loadTone(Vol vol)` | maps flight load to a display color |

#### Data bootstrap

| Method | Brief role |
|---|---|
| `initDemoData()` | seeds demo aircraft, pilots, passengers, agents, runways, flights, reservations, and one assigned runway |

### `src/test/java/airport/AirportWorkflowSmokeTest`

Purpose: verifies the main business workflows with JUnit.

| Method | Brief role |
|---|---|
| `addFormsPopulateAirportCollections()` | tests adding aircraft, pilot, passenger, agent, and runway |
| `createFlightFormWorkflowRegistersServiceAndAllowsReservation()` | tests flight creation, reservation, and status update |
| `createFlightRejectsMaintenanceAircraft()` | tests maintenance rule enforcement |
| `createFlightRejectsUnderqualifiedPilotOnLongHaul()` | tests long-haul qualification rule |
| `maintenanceAndRunwayFormsApplyOperationalChanges()` | tests maintenance toggling and runway assignment/release |
| `seededAirport()` | builds a reusable test fixture airport |

## Key Design Choices

- in-memory collections keep the project simple and classroom-friendly
- `GestionAeroport` acts as the main orchestration point instead of scattering logic across the UI
- domain constraints are expressed through custom exceptions
- the UI intentionally uses many small helper methods to keep layout code readable
- searchable selectors reduce fragile manual ID typing in forms

## Current Limitations

- there is no persistence layer or database
- the UI layer is still a single large JavaFX class
- some page updates directly modify domain objects from the UI
- test coverage is smoke-level, not exhaustive

## Suggested Next Refactors

- extract UI sections into separate screen/component classes
- introduce a dedicated application-service layer between UI and domain entities
- move direct UI-triggered entity mutations behind service methods
- add persistence if the project needs saved state
- expand tests around edge cases and UI interactions
