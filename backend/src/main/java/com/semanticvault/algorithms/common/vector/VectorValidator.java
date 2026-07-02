package com.semanticvault.algorithms.common.vector;

import com.semanticvault.algorithms.common.SearchException;

/**
 * Utility class for validating mathematical vectors.
 * Ensures data integrity before executing distance calculations or indexing.
 */
public final class VectorValidator {

    private VectorValidator() {
        // Prevent instantiation of utility class
    }

    /**
     * Validates that a single vector is non-null, non-empty, and contains only finite values.
     *
     * @param vector the vector values to validate
     * @throws SearchException if validation fails
     */
    public static void validate(float[] vector) {
        if (vector == null) {
            throw new SearchException("Vector cannot be null");
        }
        if (vector.length == 0) {
            throw new SearchException("Vector cannot be empty");
        }
        for (float val : vector) {
            if (Float.isNaN(val)) {
                throw new SearchException("Vector contains NaN value");
            }
            if (Float.isInfinite(val)) {
                throw new SearchException("Vector contains infinite value");
            }
        }
    }

    /**
     * Validates that a vector matches an expected dimension.
     *
     * @param vector the vector values to validate
     * @param expectedDimension the expected length of the vector
     * @throws SearchException if dimension mismatch is detected
     */
    public static void validateDimension(float[] vector, int expectedDimension) {
        validate(vector);
        if (vector.length != expectedDimension) {
            throw new SearchException("Vector dimension mismatch. Expected: "
                    + expectedDimension + ", got: " + vector.length);
        }
    }

    /**
     * Validates that two vectors have matching dimensions for similarity comparison.
     *
     * @param a the first vector
     * @param b the second vector
     * @throws SearchException if dimensions do not match
     */
    public static void validateMatchingDimensions(Vector a, Vector b) {
        if (a == null || b == null) {
            throw new SearchException("Vectors to compare cannot be null");
        }
        if (a.getDimension() != b.getDimension()) {
            throw new SearchException("Cannot compare vectors of different dimensions. Vector A: "
                    + a.getDimension() + ", Vector B: " + b.getDimension());
        }
    }
}
