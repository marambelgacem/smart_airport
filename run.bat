@echo off
REM ============================================================
REM  SmartAirport — Script de compilation et lancement (Windows)
REM  Chemin JavaFX : C:\Users\Acer\Downloads\javafx-sdk-26.0.1\lib
REM ============================================================

SET JAVAFX=C:\Users\Acer\Downloads\javafx-sdk-26.0.1\lib
SET SRC=src
SET OUT=out
SET MAIN=airport.ui.AirportApp

echo.
echo  SmartAirport — Compilation en cours...
echo  =========================================

REM Creer le dossier de sortie
if not exist %OUT% mkdir %OUT%

REM Compiler tous les fichiers Java
javac --module-path "%JAVAFX%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -d %OUT% ^
      %SRC%\airport\exception\VolCompletException.java ^
      %SRC%\airport\exception\VolIndisponibleException.java ^
      %SRC%\airport\exception\AvionEnMaintenanceException.java ^
      %SRC%\airport\exception\PiloteNonQualifieException.java ^
      %SRC%\airport\model\personnel\Personne.java ^
      %SRC%\airport\model\personnel\Authentifiable.java ^
      %SRC%\airport\model\personnel\Notifiable.java ^
      %SRC%\airport\model\personnel\Pilote.java ^
      %SRC%\airport\model\personnel\AgentSol.java ^
      %SRC%\airport\model\personnel\Passager.java ^
      %SRC%\airport\model\avion\Avion.java ^
      %SRC%\airport\model\vol\StatutVol.java ^
      %SRC%\airport\model\vol\Vol.java ^
      %SRC%\airport\model\service\Piste.java ^
      %SRC%\airport\collection\GestionAeroport.java ^
      %SRC%\airport\ui\AirportApp.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo  ERREUR de compilation ! Verifiez les chemins.
    pause
    exit /b 1
)

echo  Compilation reussie !
echo.
echo  Lancement de l'interface graphique...
echo  =========================================

java --module-path "%JAVAFX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp %OUT% %MAIN%

pause