-- V1__Initial_Schema.sql

-- 1. Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. User Roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- 3. User Profiles table
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    bio VARCHAR(500),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4. Refresh Tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 5. Workspaces table
CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    total_documents INTEGER DEFAULT 0,
    total_vectors INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 6. Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    title VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT DEFAULT 0,
    upload_time TIMESTAMP,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    failure_reason VARCHAR(1000),
    processing_start TIMESTAMP,
    processing_end TIMESTAMP,
    total_chunks INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_documents_workspace_id ON documents(workspace_id);

-- 7. Document Chunks table
CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id),
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER DEFAULT 0,
    start_char INTEGER DEFAULT 0,
    end_char INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);

-- 8. Vector Embeddings table
CREATE TABLE vector_embeddings (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGINT NOT NULL UNIQUE REFERENCES document_chunks(id),
    model_name VARCHAR(255) NOT NULL,
    dimensions INTEGER NOT NULL,
    vector_data BYTEA,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_embeddings_chunk_id ON vector_embeddings(chunk_id);

-- 9. Chat Sessions table
CREATE TABLE chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 10. Chat Messages table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES chat_sessions(id),
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    response_time BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 11. Search History table
CREATE TABLE search_histories (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    query TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    execution_time BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
