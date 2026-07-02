package com.semanticvault.repository;

import com.semanticvault.model.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class DocumentRepository {

    private final Map<Long, Document> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(idGenerator.getAndIncrement());
        }
        store.put(document.getId(), document);
        return document;
    }

    public Optional<Document> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    public List<Document> findByWorkspaceId(Long workspaceId) {
        if (workspaceId == null) {
            return new ArrayList<>();
        }
        return store.values().stream()
                .filter(doc -> workspaceId.equals(doc.getWorkspaceId()))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
}
