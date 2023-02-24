package com.arassec.artivact.common.exception;

public class ArtivactException extends RuntimeException {

    public ArtivactException(String message) {
        super(message);
    }

    public ArtivactException(String message, Throwable cause) {
        super(message, cause);
    }

}
