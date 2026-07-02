package com.semanticvault.algorithms.common.vector;

/**
 * Immutable representation of a numerical vector embedding.
 * Ensures internal arrays cannot be modified post-creation.
 */
public final class Vector {

    private final Long id;
    private final float[] values;
    private final int dimension;
    private final Long metadataId;

    /**
     * Constructs a Vector with validation.
     * Deep copies the input values to maintain immutability.
     *
     * @param id internal database ID or index identifier
     * @param values raw vector values
     * @param metadataId associated metadata record ID
     */
    public Vector(Long id, float[] values, Long metadataId) {
        VectorValidator.validate(values);
        this.id = id;
        this.values = VectorUtils.copy(values);
        this.dimension = values.length;
        this.metadataId = metadataId;
    }

    public Long getId() {
        return id;
    }

    /**
     * Returns a copy of the underlying vector values to preserve immutability.
     */
    public float[] getValues() {
        return VectorUtils.copy(values);
    }

    public int getDimension() {
        return dimension;
    }

    public Long getMetadataId() {
        return metadataId;
    }

    /**
     * Calculates the Euclidean magnitude of this vector.
     *
     * @return magnitude
     */
    public double magnitude() {
        return VectorUtils.magnitude(values);
    }

    /**
     * Returns a new Vector containing normalized values.
     *
     * @return new unit-length Vector
     */
    public Vector normalize() {
        float[] normalizedValues = VectorUtils.normalize(values);
        return new Vector(id, normalizedValues, metadataId);
    }
}
