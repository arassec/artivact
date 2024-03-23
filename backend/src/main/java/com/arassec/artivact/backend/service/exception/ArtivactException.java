package com.arassec.artivact.backend.service.exception;

/**
 * Exception thrown throughout the application.
 */
public class ArtivactException extends RuntimeException {

    /**
     * Creates a new instance.
     *
     * @param message The exception's message.
     */
    public ArtivactException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     *
     * @param message The exception's message.
     * @param cause   The exception's original cause.
     */
    public ArtivactException(String message, Throwable cause) {
        super(message, cause);
    }

}
