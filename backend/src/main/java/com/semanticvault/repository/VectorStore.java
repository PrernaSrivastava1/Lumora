package com.semanticvault.repository;

import com.semanticvault.algorithms.common.vector.Vector;
import com.semanticvault.index.IndexManager;
import com.semanticvault.index.VectorIndex;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Thread-safe, workspace-aware repository for storing and managing high-dimensional vectors.
 * Delegates to IndexManager for workspace-partitioned in-memory vector storage.
 */
@Repository
public class VectorStore {

    private final IndexManager indexManager;
    private static final Long DEFAULT_WORKSPACE_ID = 1L;

    public VectorStore(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Gets the underlying IndexManager.
     */
    public IndexManager getIndexManager() {
        return indexManager;
    }

    /**
     * Adds a vector to the default workspace in-memory store.
     *
     * @param vector the vector to store
     */
    public void add(Vector vector) {
        add(DEFAULT_WORKSPACE_ID, vector);
    }

    /**
     * Adds a vector to a workspace-specific in-memory store.
     *
     * @param workspaceId the workspace identifier
     * @param vector the vector to store
     */
    public void add(Long workspaceId, Vector vector) {
        if (workspaceId != null && vector != null && vector.getId() != null) {
            VectorIndex index = indexManager.getOrCreateIndex(workspaceId);
            index.add(vector);
        }
    }

    /**
     * Removes a vector from the default workspace store by its ID.
     *
     * @param id identification of the vector to remove
     * @return true if a vector was removed, false otherwise
     */
    public boolean remove(Long id) {
        return remove(DEFAULT_WORKSPACE_ID, id);
    }

    /**
     * Removes a vector from a workspace-specific store by its ID.
     *
     * @param workspaceId the workspace identifier
     * @param id identification of the vector to remove
     * @return true if a vector was removed, false otherwise
     */
    public boolean remove(Long workspaceId, Long id) {
        if (workspaceId == null || id == null) {
            return false;
        }
        VectorIndex index = indexManager.getIndex(workspaceId);
        return index != null && index.remove(id);
    }

    /**
     * Finds a vector by its unique ID in the default workspace.
     *
     * @param id the vector identifier
     * @return the vector, or null if not found
     */
    public Vector findById(Long id) {
        return findById(DEFAULT_WORKSPACE_ID, id);
    }

    /**
     * Finds a vector by its unique ID in a workspace-specific store.
     *
     * @param workspaceId the workspace identifier
     * @param id the vector identifier
     * @return the vector, or null if not found
     */
    public Vector findById(Long workspaceId, Long id) {
        if (workspaceId == null || id == null) {
            return null;
        }
        VectorIndex index = indexManager.getIndex(workspaceId);
        return index != null ? index.get(id) : null;
    }

    /**
     * Returns all stored vectors in the default workspace.
     *
     * @return a list of all vectors in the store
     */
    public List<Vector> findAll() {
        return findAll(DEFAULT_WORKSPACE_ID);
    }

    /**
     * Returns all stored vectors in a workspace-specific store.
     *
     * @param workspaceId the workspace identifier
     * @return a list of all vectors in the workspace index
     */
    public List<Vector> findAll(Long workspaceId) {
        if (workspaceId == null) {
            return Collections.emptyList();
        }
        VectorIndex index = indexManager.getIndex(workspaceId);
        return index != null ? index.getAll() : Collections.emptyList();
    }

    /**
     * Counts the total number of vectors in the default workspace.
     *
     * @return total count of vectors
     */
    public int count() {
        return count(DEFAULT_WORKSPACE_ID);
    }

    /**
     * Counts the total number of vectors in a workspace-specific store.
     *
     * @param workspaceId the workspace identifier
     * @return total count of vectors in the workspace index
     */
    public int count(Long workspaceId) {
        if (workspaceId == null) {
            return 0;
        }
        VectorIndex index = indexManager.getIndex(workspaceId);
        return index != null ? index.count() : 0;
    }

    /**
     * Clears all stored vectors in the default workspace.
     */
    public void clear() {
        clear(DEFAULT_WORKSPACE_ID);
    }

    /**
     * Clears all stored vectors in a workspace-specific store.
     *
     * @param workspaceId the workspace identifier
     */
    public void clear(Long workspaceId) {
        if (workspaceId != null) {
            VectorIndex index = indexManager.getIndex(workspaceId);
            if (index != null) {
                index.clear();
            }
        }
    }
}
