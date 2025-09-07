@echo off
echo Starting Spring Boot Application in LOCAL mode...
echo Using SQLite database and local SMTP
echo.
cd springboot
mvn spring-boot:run
pause
