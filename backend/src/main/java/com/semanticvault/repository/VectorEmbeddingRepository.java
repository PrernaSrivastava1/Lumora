package com.semanticvault.repository;

import com.semanticvault.model.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, Long> {

    Optional<VectorEmbedding> findByChunkId(Long chunkId);

    void deleteByChunkId(Long chunkId);

    @Query("SELECT ve FROM VectorEmbedding ve WHERE ve.chunk.document.workspace.id = :workspaceId")
    List<VectorEmbedding> findByWorkspaceId(@Param("workspaceId") Long workspaceId);
}

