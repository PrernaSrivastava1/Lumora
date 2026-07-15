# Lumora: Developer-First Semantic Search & Vector Engine

Lumora is a lightweight, self-hostable vector search and document retrieval system designed to run entirely on your local machine. It combines custom indexing data structures written in Java with a responsive React dashboard, allowing developers to experiment with vector math, measure query latencies, and search document contents without relying on third-party cloud APIs.

## Why I Built Lumora
Most vector databases are complex to run locally and act as "black boxes" when doing similarity matches. I built Lumora to peel back these layers. By implementing traditional spatial indexing (KD-Tree) and modern graph routing (Hierarchical Navigable Small World, or HNSW) directly in clean Java, this project serves as both an educational playground and a practical local search utility. It is aimed at software engineer students, AI hobbyists, and developers who want a quick, transparent, and offline search engine.

---

## What Lumora Does (Key Features)

- **Isolated Workspaces**: You can group your data into logical containers called Workspaces. This keeps your experiments separate (e.g., testing one document collection without polluting another).
- **Multi-Algorithm Vector Search**: Supports three search strategies that you can swap on the fly:
  - **Brute Force (Exact KNN)**: Exhaustively computes similarity scores against every vector. It is slow for large sets but serves as a ground-truth benchmark for accuracy.
  - **KD-Tree Search**: Partitions vectors along coordinate axes. Great for low-dimensional exact spatial queries.
  - **HNSW (Hierarchical Navigable Small World)**: Creates a multi-layer network graph. High-speed approximation suitable for high-dimensional text embeddings.
- **Smart AUTO Routing**: If you don't know which search algorithm to choose, setting the search mode to `AUTO` lets Lumora select the best fit dynamically based on how many vectors exist in your workspace.
- **Natural Text Slicing (Chunking)**: When you upload documents (TXT, MD, PDF, or DOCX), Lumora doesn't just store the raw file. It divides the text into readable chunks so that search matches return precise sentences rather than whole pages.
- **Local AI Embedding Integration**: Connects directly to local Ollama installations, translating user search queries and raw text into vectors using offline models (like `nomic-embed-text`).

---

## How Lumora Works Under the Hood

When you feed a text document into Lumora, it follows a structured processing pipeline:

```
┌─────────────────┐
│ Upload Document │ (User drags & drops a TXT, MD, PDF, or DOCX file)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Text Slicing    │ (Slices text into overlapping 250-word chunks)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Vectorize Text  │ (Sends chunks to local Ollama API to generate embeddings)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Store & Index   │ (Saves metadata in database; populates HNSW graph/KD-Tree)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Search Query    │ (User enters a query; resolved strategy scans vector indices)
└─────────────────┘
```

---

## The Tech Stack

- **Java 21 & Spring Boot**: Serves as our backend foundation. Java's modern concurrency utilities make graph traversals safe, while Spring Boot helps build type-safe REST APIs quickly.
- **React 18 & TypeScript**: Used for the frontend dashboard. TypeScript ensures compile-time safety when parsing API responses, and the interface is styled with clean CSS and Tailwind.
- **PostgreSQL**: Used for persisting workspaces, document metadata, and text chunks. By default, Lumora runs on a lightweight H2 file fallback database for easy initial setups.
- **Ollama**: Our offline local model server. It ensures your data never leaves your laptop during embedding generations.
- **Docker**: Optional setup path for running isolated databases or backend servers.

---

## Project Structure

```
Lumora/
├── backend/                  # Spring Boot application
│   ├── src/main/java         # Java source files (Algorithms, Services, Controllers)
│   ├── src/test/java         # JUnit integration and unit tests
│   └── pom.xml               # Maven configuration and dependencies
├── frontend/                 # React frontend
│   ├── src/components        # UI widgets and layout views
│   ├── src/pages             # Search, Document Upload, and Dashboard pages
│   └── vite.config.ts        # Vite dev server and API proxy setup
├── docs/                     # Design documents and walkthroughs
├── docker-compose.yml        # Setup configuration for PostgreSQL and backend containers
├── start-dev.ps1             # One-command startup script for Windows users
└── README.md                 # This guide
```

---

## Quick Setup and Run

### 1. Prerequisites
Make sure you have these tools ready on your machine:
- **Java Development Kit (JDK) 21**
- **Node.js** (v18 or newer)
- **Ollama** (Verify it is running and pull the embedding model: `ollama pull nomic-embed-text`)

### 2. Automatic Startup (Windows)
We provide a PowerShell script to run everything. Just run this command from the root of the project:
```powershell
.\start-dev.ps1
```
This opens two shell windows: one building and launching the Spring Boot backend, and the other running the Vite development server.

### 3. Manual Separate Commands

If you prefer launching components manually, open separate terminal windows and run:

**Backend**:
```bash
cd backend
mvn spring-boot:run
```

**Frontend**:
```bash
cd frontend
npm install
npm run dev
```

---

## Core URLs & Endpoints

- **Frontend Interface**: [http://localhost:5173](http://localhost:5173)
- **REST Base API**: [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger Docs**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Health check endpoint**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## Authentication & User Session Flow

Lumora uses a production-ready JWT stateless authentication flow with sliding-window token renewals:
- **Registration**: `/api/v1/auth/register` creates a user account and hashes the password using `BCryptPasswordEncoder`.
- **Log In**: `/api/v1/auth/login` checks credentials and issues a short-lived Access Token (JWT) alongside a UUID Refresh Token stored in the database.
- **Access Control**: Reusable React Context (`AuthContext.tsx`) and React Router guards (`ProtectedRoute.tsx`) redirect unauthorized users to the `/login` page.
- **Silent Refresh**: Axios interceptors intercept `401 Unauthorized` responses and exchange the active Refresh Token for a new Access Token automatically.
- **Log Out**: `/api/v1/auth/logout` terminates the session and deletes the active Refresh Token from the database.

---

## Retrieval-Augmented Generation (RAG) Flow

Lumora implements a complete local RAG query pipeline powered by its custom Java indexing engine and offline Ollama LLMs:
1. **User Input**: A user inputs a natural language question in the AI Chat interface.
2. **Context Retrieval**: The backend generates a query embedding and executes a vector similarity search across the selected workspace index.
3. **Prompt Synthesis**: The `PromptBuilder` retrieves the top-K document chunk matches and constructs a contextual system prompt.
4. **Answer Generation**: The prompt is dispatched to a local Ollama server (running `llama3` or similar model) which synthesizes a markdown-formatted response.
5. **Citations Display**: The generated answer is presented in the chat along with clickable references to the exact source chunks used.

---

## API Quick Reference

### 1. User Registration
- **Method**: `POST`
- **Endpoint**: `/api/v1/auth/register`
- **Payload**:
  ```json
  {
    "username": "developer",
    "email": "dev@lumora.ai",
    "password": "securepassword"
  }
  ```

### 2. User Login
- **Method**: `POST`
- **Endpoint**: `/api/v1/auth/login`
- **Payload**:
  ```json
  {
    "username": "developer",
    "password": "securepassword"
  }
  ```

### 3. Create Workspace
- **Method**: `POST`
- **Endpoint**: `/api/v1/workspaces`
- **Payload**:
  ```json
  {
    "name": "Research Papers",
    "description": "Store vector embeddings of math documents"
  }
  ```

### 4. Upload Document
- **Method**: `POST`
- **Endpoint**: `/api/v1/documents/upload`
- **Payload**: `multipart/form-data` with files and `workspaceId` fields.

### 5. Execute Vector Search
- **Method**: `POST`
- **Endpoint**: `/api/v1/search`
- **Payload**:
  ```json
  {
    "workspaceId": 1,
    "query": "artificial intelligence",
    "algorithm": "AUTO",
    "metric": "COSINE",
    "topK": 5
  }
  ```

---

## Communication Architecture

```
┌──────────────────┐               ┌──────────────────┐
│ React Frontend   │  HTTP / JSON  │ Spring Backend   │
│ (Vite Port 5173) ├──────────────►│ (Tomcat Port 8080)│
└──────────────────┘               └────────┬────────┬┘
                                            │        │
                                 JDBC Query │        │ HTTP Request
                                            ▼        ▼
                               ┌──────────────┐    ┌──────────────┐
                               │ Database     │    │ Local Ollama │
                               │ (H2 / PG)    │    │ (Port 11434) │
                               └──────────────┘    └──────────────┘
```

---

## Product Demo Guide (Preseeded Reviewer Flow)

Lumora comes preloaded with a default **Demo Workspace** containing pre-chunked resumes and system specs. This allows reviewers to explore 100% of the platform features without having to install Ollama or upload custom files!

### Step-by-Step Demo Guide:

1. **Log In / Register**:
   - Go to [http://localhost:5173](http://localhost:5173).
   - Click **Register** and create a user (e.g., `reviewer`).
   - Log in using your credentials.
2. **Explore Preloaded Workspace Statistics**:
   - Navigate to the **Analytics** page.
   - Select **Demo Workspace** in the stats dropdown.
   - Review preseeded metrics showing document counts, segments, average processing bounds, and active counters.
3. **Trigger On-Demand Benchmarking**:
   - Navigate to the **Benchmark** page.
   - Enter a query (e.g., `candidate skills`) and select **Demo Workspace**.
   - Click **Run Comparative Suite**.
   - Review the newly drawn interactive bar charts comparing **Latency** (ms) and **Accuracy/Recall** (%) side-by-side.
4. **Vector Search with Document Viewer**:
   - Navigate to the **Vector Search** page.
   - Enter `React Developer` or search terms (test synonym expansion by typing `backend` or `frontend`).
   - Review matching search hits with rank and confidence values.
   - Click the **Open** link on a hit.
   - The app navigates to the **Document Viewer**, highlights the exact matching segment, and scrolls it into view.
5. **AI Chat with Memory & Suggestions**:
   - Navigate to the **AI Chat** page.
   - Notice the **Smart AI Suggestions** chips at the bottom (e.g., "Summarize this workspace", "What skills does john doe resume have?"). Click any suggestion to submit it.
   - Ask follow-up questions. Click **Memory Setup** to toggle/customize chat history turn limits.
   - Click **Why this answer?** to inspect retrieval scores.
   - Export your conversation by clicking the **MD** or **PDF** export buttons in the header.
