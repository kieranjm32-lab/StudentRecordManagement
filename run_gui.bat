@echo off
cd /d "%~dp0"
javac *.java
if errorlevel 1 (
  echo Compile failed. Is the Java JDK installed?
  pause
  exit /b 1
)
java StudentApp
