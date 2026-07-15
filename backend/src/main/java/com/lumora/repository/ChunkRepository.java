package com.lumora.repository;

import com.lumora.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);

    List<DocumentChunk> findByDocumentWorkspaceId(Long workspaceId);

    long countByDocumentWorkspaceId(Long workspaceId);
}
