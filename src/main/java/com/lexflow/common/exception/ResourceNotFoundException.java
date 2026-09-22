package com.lexflow.common.exception;

/**
 * Thrown when a requested entity does not exist.
 * The REST layer translates it into HTTP 404 (see ApiExceptionHandler).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
