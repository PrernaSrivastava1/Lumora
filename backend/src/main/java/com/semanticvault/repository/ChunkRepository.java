package com.semanticvault.repository;

import com.semanticvault.model.DocumentChunk;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ChunkRepository {

    private final Map<Long, DocumentChunk> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<DocumentChunk> saveAll(List<DocumentChunk> chunks) {
        if (chunks == null) {
            return new ArrayList<>();
        }
        for (DocumentChunk chunk : chunks) {
            if (chunk.getId() == null) {
                chunk.setId(idGenerator.getAndIncrement());
            }
            store.put(chunk.getId(), chunk);
        }
        return chunks;
    }

    public List<DocumentChunk> findByDocumentId(Long documentId) {
        if (documentId == null) {
            return new ArrayList<>();
        }
        return store.values().stream()
                .filter(chunk -> documentId.equals(chunk.getDocumentId()))
                .collect(Collectors.toList());
    }

    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) return;
        store.values().removeIf(chunk -> documentId.equals(chunk.getDocumentId()));
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
}
