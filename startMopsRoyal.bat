@echo off
echo ========================================
echo    Mops Royal wird gestartet...
echo ========================================
echo.

REM Prüfe ob Java 21 verfügbar ist
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo FEHLER: Java ist nicht installiert oder nicht im PATH!
    echo Bitte Java 21 installieren: https://adoptium.net/
    pause
    exit /b 1
)

REM Zeige Java-Version
echo Verwende Java-Version:
java -version
echo.

cd PP_MopsRoyal_Gaethke\target
if not exist PP_MopsRoyal_Gaethke-1.0-SNAPSHOT.jar (
    echo FEHLER: JAR-Datei nicht gefunden!
    echo Bitte erst mit Maven bauen:
    echo 1. In IntelliJ: Maven Tool Window ^> Lifecycle ^> package
    pause
    exit /b 1
)

REM Starte das Spiel
java -jar PP_MopsRoyal_Gaethke-1.0-SNAPSHOT.jar

if errorlevel 1 (
    echo.
    echo ========================================
    echo FEHLER beim Starten!
    echo ========================================
    echo.
    echo Mögliche Ursachen:
    echo 1. Falsche Java-Version (benoetigt Java 21)
    echo 2. Aktuelle Java-Version pruefen mit: java -version
    echo 3. Java 21 installieren von: https://adoptium.net/
    echo Für eine detaillierte Lösungsbeschreibung siehe Kapitel 1.4 Fehlermeldungen in Dokumentation
    echo.
)
pause