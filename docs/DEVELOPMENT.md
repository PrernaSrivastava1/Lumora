# Development Guide

This document describes how to set up, run, and develop the Lumora codebase.

## Local Machine Setup

### Prerequisites
1. **Java JDK 21**: Make sure `java -version` returns JDK 21.
2. **Node.js (v18+)**: Make sure `node -v` returns v18+.
3. **Ollama**: Required for running the embedding provider. Run `ollama pull nomic-embed-text` to pull the active model.

### Launching the Application
Use the Windows PowerShell script:
```powershell
.\start-dev.ps1
```
Or run backend and frontend separately:
- Backend: `mvn spring-boot:run` in `/backend`
- Frontend: `npm run dev` in `/frontend`

## Working with Profiles
Lumora supports three Spring profiles:
- `dev`: Standard development profile using local H2 file database. (Default)
- `test`: JUnit testing profile using H2 in-memory database.
- `prod`: Production profile using PostgreSQL. Set database details using environmental properties `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
