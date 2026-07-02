package com.semanticvault.algorithms.common.vector;

import com.semanticvault.algorithms.common.SearchException;

import java.util.Arrays;

/**
 * Mathematical utilities for raw float array operations.
 */
public final class VectorUtils {

    private VectorUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Calculates the dot product of two raw vectors.
     *
     * @param a first vector values
     * @param b second vector values
     * @return dot product
     */
    public static double dotProduct(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new SearchException("Vector lengths do not match for dot product calculation");
        }
        double dot = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }

    /**
     * Calculates the Euclidean magnitude of a raw vector.
     *
     * @param vector the vector values
     * @return Euclidean magnitude (L2 norm)
     */
    public static double magnitude(float[] vector) {
        double sum = 0.0;
        for (float val : vector) {
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    /**
     * Normalizes a raw vector (makes it unit length L2 norm).
     * Returns a new array.
     *
     * @param vector the vector values
     * @return a new normalized float array
     */
    public static float[] normalize(float[] vector) {
        double mag = magnitude(vector);
        if (mag < 1e-9) {
            // Avoid division by zero, return zero array or same
            return copy(vector);
        }
        float[] norm = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            norm[i] = (float) (vector[i] / mag);
        }
        return norm;
    }

    /**
     * Creates a deep copy of the raw float vector array.
     *
     * @param vector raw float array to copy
     * @return deep copy of float array
     */
    public static float[] copy(float[] vector) {
        if (vector == null) {
            return null;
        }
        return Arrays.copyOf(vector, vector.length);
    }

    /**
     * Parses a comma-separated string of numbers into a raw float array.
     *
     * @param query comma-separated string representation of a vector
     * @return parsed float array
     * @throws SearchException if input string is null, empty, or contains malformed numbers
     */
    public static float[] parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new SearchException("Vector query string cannot be null or empty");
        }
        String[] tokens = query.split(",");
        float[] vector = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                vector[i] = Float.parseFloat(tokens[i].trim());
            } catch (NumberFormatException e) {
                throw new SearchException("Malformed vector element: " + tokens[i], e);
            }
        }
        return vector;
    }
}
