@echo off
set JAVA_HOME=D:\JDK\jdk-23.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_PATH=D:\JavaFX\javafx-sdk-23.0.1\lib

echo Compilation des fichiers Java...
mkdir build 2>nul
javac -d build -cp "%JAVAFX_PATH%/*" src/*.java

echo Lancement du jeu Gomoku...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp build src.GomokuApp

pause 