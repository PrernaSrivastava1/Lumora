package com.semanticvault.repository;

import com.semanticvault.model.Workspace;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class WorkspaceRepository {

    private final Map<Long, Workspace> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public WorkspaceRepository() {
        // Preload default demo workspace
        Workspace defaultWorkspace = Workspace.builder()
                .id(idGenerator.getAndIncrement())
                .name("System Demo Index")
                .description("Default namespace loaded with 16-dimensional categorical vector items.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalDocuments(0)
                .totalVectors(20)
                .build();
        store.put(defaultWorkspace.getId(), defaultWorkspace);
    }

    public Workspace save(Workspace workspace) {
        if (workspace.getId() == null) {
            workspace.setId(idGenerator.getAndIncrement());
        }
        store.put(workspace.getId(), workspace);
        return workspace;
    }

    public Optional<Workspace> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    public List<Workspace> findAll() {
        return new ArrayList<>(store.values());
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
