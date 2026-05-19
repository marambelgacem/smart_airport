# Smart Airport

Portable JavaFX airport management demo application.

## Requirements

- Java 17 or newer installed
- Internet access on the first run so Maven can download JavaFX dependencies

## Run the project

### Windows

```bat
run.bat
```

### macOS / Linux

```bash
chmod +x run.sh
./run.sh
```

The launcher script will:

- use Maven if it is already installed on your machine
- otherwise download Apache Maven locally into `.tools/`
- download JavaFX dependencies automatically through Maven
- align `JAVA_HOME` with the detected `java` executable
- start the JavaFX application

## Manual Maven commands

If Maven is already installed, you can also run the app directly:

```bash
mvn clean javafx:run
```

To build the project without launching it:

```bash
mvn clean package
```

## Project notes

- The project no longer depends on a hardcoded JavaFX SDK path.
- The project is now modular, which avoids the IntelliJ classpath warning for JavaFX.
- The source compile issues from the original repository were fixed.
- Demo data now includes ground staff so the personnel polymorphism screen is fully populated.
