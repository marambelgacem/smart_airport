# Smart Airport

Smart Airport is a modular JavaFX desktop application for managing a small airport domain: flights, aircraft, pilots, passengers, runways, and ground staff.

The project mixes:

- a domain layer built around plain Java classes
- a service layer centered on `GestionAeroport`
- a JavaFX UI in `AirportApp`
- smoke tests for the main operational workflows

## What the app does

- creates and tracks flights
- assigns aircraft and pilots to services
- reserves passengers on flights
- updates flight status
- manages aircraft maintenance state
- adds runways and assigns or releases them
- shows a premium operations dashboard with summary views

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- JUnit 5

## Project Structure

```text
src/
├── module-info.java
├── airport/
│   ├── MainLauncher.java
│   ├── collection/
│   │   └── GestionAeroport.java
│   ├── exception/
│   │   ├── AvionEnMaintenanceException.java
│   │   ├── PiloteNonQualifieException.java
│   │   ├── VolCompletException.java
│   │   ├── VolIndisponibleException.java
│   │   └── VolInexistantException.java
│   ├── model/
│   │   ├── avion/
│   │   │   └── Avion.java
│   │   ├── personnel/
│   │   │   ├── AgentSol.java
│   │   │   ├── Authentifiable.java
│   │   │   ├── Notifiable.java
│   │   │   ├── Passager.java
│   │   │   ├── Personne.java
│   │   │   └── Pilote.java
│   │   ├── service/
│   │   │   └── Piste.java
│   │   └── vol/
│   │       ├── StatutVol.java
│   │       └── Vol.java
│   └── ui/
│       └── AirportApp.java
└── test/
    └── java/
        └── airport/
            └── AirportWorkflowSmokeTest.java
```

## Requirements

- Java 17 or newer
- internet access on first launch if Maven still needs to be downloaded or dependencies still need to be resolved

## Run the App

### Windows

```bat
run.bat
```

### macOS / Linux

```bash
chmod +x run.sh
./run.sh
```

## What the launcher scripts do

- detect `java` from your machine
- set `JAVA_HOME` from the detected Java installation
- use local Maven if it already exists
- otherwise download Apache Maven into `.tools/`
- run `mvn clean javafx:run`

## Run with Maven directly

```bash
mvn clean javafx:run
```

## Build the project

```bash
mvn clean package
```

## Run tests

```bash
mvn test
```

The current smoke tests focus on the main operational flows:

- adding core entities
- creating flights
- reserving passengers
- validating pilot and aircraft constraints
- maintenance and runway workflows

## Main Concepts in the Codebase

- `GestionAeroport` is the central coordinator for airport operations
- `Vol`, `Avion`, `Pilote`, `Passager`, `AgentSol`, and `Piste` model the business domain
- custom exceptions express business-rule failures clearly
- `AirportApp` builds the full JavaFX interface and binds UI actions to service methods
- the UI includes searchable selectors for entity-driven forms

## Notes

- the app is modular through `module-info.java`
- JavaFX is managed by Maven, not a hardcoded SDK path
- demo data is loaded at startup for easier exploration
- the UI was reworked into an operations-desk style dashboard

## Documentation

- See `ARCHITECTURE.md` for the package layout, class responsibilities, and a brief method-by-method reference.
