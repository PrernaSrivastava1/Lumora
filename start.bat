@echo off
echo ====================================================
echo   LUMORA Vector Database - Starting Servers
echo ====================================================
echo.

echo [1/2] Starting Backend (Spring Boot on port 8080)...
start "Lumora Backend" cmd /k "cd /d "%~dp0backend" && mvnw spring-boot:run"

echo [2/2] Starting Frontend (Vite on port 5173)...
start "Lumora Frontend" cmd /k "cd /d "%~dp0frontend" && npm run dev"

echo.
echo ====================================================
echo  Backend  --^>  http://localhost:8080
echo  Frontend --^>  http://localhost:5173
echo ====================================================
echo.
echo Both servers are launching in separate windows.
echo Wait ~30 seconds for the backend to fully start,
echo then open http://localhost:5173 in your browser.
echo.
pause
