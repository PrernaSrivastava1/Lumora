package com.lumora.algorithms.common;

/**
 * Exception thrown when a search operation fails, validation fails,
 * or a requested search algorithm is unavailable.
 */
public class SearchException extends RuntimeException {

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
