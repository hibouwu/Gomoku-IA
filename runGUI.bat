@echo off
set JAVA_HOME=D:\JDK\jdk-23.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=D:\javafx\javafx-sdk-23.0.1

echo Compilation des fichiers GUI...
mkdir bin 2>nul

echo Compilation de GomokuApp...
javac --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.graphics -d bin src/*.java

if %errorlevel% neq 0 (
    echo Erreur de compilation. Vérifiez que JavaFX est correctement installé à l'emplacement: %JAVAFX_HOME%
    goto end
)

echo Démarrage de l'interface graphique...
java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.graphics -cp bin src.GomokuApp

:end
pause 