@echo off
set JAVA_HOME=D:\JDK\jdk-23.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=D:\javafx\javafx-sdk-23.0.1

echo 正在编译...
javac --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.graphics -d bin src/*.java

echo 正在运行...
java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.graphics -cp bin src.GomokuApp

pause 