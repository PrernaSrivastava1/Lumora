package com.semanticvault.algorithms.common.vector;

/**
 * Interface representing a strategy to calculate distance between two vectors.
 */
public interface DistanceCalculator {

    /**
     * Calculates the distance between two vectors.
     * Higher return values represent greater distance (lower similarity).
     *
     * @param a the first vector
     * @param b the second vector
     * @return calculated distance value
     */
    double calculate(Vector a, Vector b);
}
