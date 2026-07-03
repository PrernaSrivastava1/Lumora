package com.lumora.algorithms.common.vector;

/**
 * Calculates Manhattan (L1) Distance between two vectors.
 */
public class ManhattanDistanceCalculator implements DistanceCalculator {

    @Override
    public double calculate(Vector a, Vector b) {
        VectorValidator.validateMatchingDimensions(a, b);

        float[] aVals = a.getValues();
        float[] bVals = b.getValues();
        double sum = 0.0;

        for (int i = 0; i < aVals.length; i++) {
            sum += Math.abs(aVals[i] - bVals[i]);
        }

        return sum;
    }
}
