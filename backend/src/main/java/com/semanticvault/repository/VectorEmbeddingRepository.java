package com.semanticvault.repository;

import com.semanticvault.model.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, Long> {

    Optional<VectorEmbedding> findByChunkId(Long chunkId);

    void deleteByChunkId(Long chunkId);
}
