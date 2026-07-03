package com.lumora.index;

import com.lumora.model.IndexStats;
import java.util.Map;

/**
 * Abstraction for managing multiple workspace-specific vector indexes.
 */
public interface IndexManager {

    /**
     * Gets an existing VectorIndex for a workspace, or creates a new one if it does not exist.
     */
    VectorIndex getOrCreateIndex(Long workspaceId);

    /**
     * Gets the VectorIndex for a workspace. Returns null if not present/loaded.
     */
    VectorIndex getIndex(Long workspaceId);

    /**
     * Deletes the VectorIndex for a workspace.
     * @return true if index existed and was deleted, false otherwise.
     */
    boolean deleteIndex(Long workspaceId);

    /**
     * Checks if a workspace has an active index in memory.
     */
    boolean hasIndex(Long workspaceId);

    /**
     * Gets statistics for a specific workspace index.
     * @throws IllegalArgumentException if the index is not found/loaded.
     */
    IndexStats getIndexStats(Long workspaceId);

    /**
     * Gets statistics for all active workspace indexes in memory.
     */
    Map<Long, IndexStats> getAllIndexStats();

    /**
     * Loads existing vectors for a workspace from persistent storage into memory.
     */
    void loadWorkspaceVectors(Long workspaceId);

    /**
     * Unloads/removes a workspace index from memory to free resources.
     */
    void unloadWorkspaceVectors(Long workspaceId);

    /**
     * Clears all indexes from memory.
     */
    void clearAll();
}
