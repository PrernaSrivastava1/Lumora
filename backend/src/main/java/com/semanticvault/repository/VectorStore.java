package com.semanticvault.repository;

import com.semanticvault.algorithms.common.vector.Vector;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory repository for storing and managing high-dimensional vectors.
 */
@Repository
public class VectorStore {

    private final Map<Long, Vector> store = new ConcurrentHashMap<>();

    /**
     * Adds a vector to the in-memory store.
     *
     * @param vector the vector to store
     */
    public void add(Vector vector) {
        if (vector != null && vector.getId() != null) {
            store.put(vector.getId(), vector);
        }
    }

    /**
     * Removes a vector from the store by its ID.
     *
     * @param id identification of the vector to remove
     * @return true if a vector was removed, false otherwise
     */
    public boolean remove(Long id) {
        if (id == null) {
            return false;
        }
        return store.remove(id) != null;
    }

    /**
     * Finds a vector by its unique ID.
     *
     * @param id the vector identifier
     * @return the vector, or null if not found
     */
    public Vector findById(Long id) {
        if (id == null) {
            return null;
        }
        return store.get(id);
    }

    /**
     * Returns all stored vectors.
     *
     * @return a list of all vectors in the store
     */
    public List<Vector> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Counts the total number of vectors in the store.
     *
     * @return total count of vectors
     */
    public int count() {
        return store.size();
    }

    /**
     * Clears all stored vectors.
     */
    public void clear() {
        store.clear();
    }
}
