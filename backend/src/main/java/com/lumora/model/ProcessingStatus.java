package com.lumora.model;

/**
 * Lifecycle status of an uploaded document.
 */
public enum ProcessingStatus {
    UPLOADED,
    VALIDATING,
    EXTRACTING_TEXT,
    CLEANING_TEXT,
    CHUNKING,
    GENERATING_EMBEDDINGS,
    INDEXING,
    READY,
    FAILED
}
