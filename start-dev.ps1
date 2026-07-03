# Lumora One-Command Local Development Startup Script
# Usage: .\start-dev.ps1

Write-Host "Starting Lumora Development Environment..." -ForegroundColor Cyan

# 1. Start Spring Boot Backend in a new window
Write-Host "Launching Backend on http://localhost:8080 ..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "$env:JAVA_HOME='C:\Program Files\Java\jdk-21'; cd backend; C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd spring-boot:run"

# 2. Start React/Vite Frontend in a new window
Write-Host "Launching Frontend on http://localhost:5173 ..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev"

Write-Host "Both services are initializing. Check the opened terminal windows for logs!" -ForegroundColor Green
