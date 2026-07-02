-- Lumora Database Schema
-- This script creates the base tables for the application.
-- Hibernate ddl-auto=update will manage schema evolution, but this provides
-- a reference script for manual PostgreSQL setup.

-- Workspaces
CREATE TABLE IF NOT EXISTS workspaces (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    total_documents INTEGER DEFAULT 0,
    total_vectors   INTEGER DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- Documents
CREATE TABLE IF NOT EXISTS documents (
    id                  BIGSERIAL PRIMARY KEY,
    workspace_id        BIGINT NOT NULL REFERENCES workspaces(id),
    title               VARCHAR(255) NOT NULL,
    original_file_name  VARCHAR(255) NOT NULL,
    file_type           VARCHAR(50),
    file_size           BIGINT DEFAULT 0,
    upload_time         TIMESTAMP,
    processing_status   VARCHAR(50) NOT NULL DEFAULT 'UPLOADING',
    total_chunks        INTEGER DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_documents_workspace_id ON documents(workspace_id);

-- Document Chunks
CREATE TABLE IF NOT EXISTS document_chunks (
    id              BIGSERIAL PRIMARY KEY,
    document_id     BIGINT NOT NULL REFERENCES documents(id),
    chunk_index     INTEGER NOT NULL,
    content         TEXT NOT NULL,
    token_count     INTEGER DEFAULT 0,
    start_char      INTEGER DEFAULT 0,
    end_char        INTEGER DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_chunks_document_id ON document_chunks(document_id);

-- Vector Embeddings
CREATE TABLE IF NOT EXISTS vector_embeddings (
    id              BIGSERIAL PRIMARY KEY,
    chunk_id        BIGINT NOT NULL UNIQUE REFERENCES document_chunks(id),
    model_name      VARCHAR(255) NOT NULL,
    dimensions      INTEGER NOT NULL,
    vector_data     BYTEA,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_embeddings_chunk_id ON vector_embeddings(chunk_id);
