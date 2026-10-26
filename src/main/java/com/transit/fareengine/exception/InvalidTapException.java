package com.transit.fareengine.exception;

/**
 * Exception thrown when a Tap event is invalid or cannot be processed.
 */
public class InvalidTapException extends RuntimeException {

    public InvalidTapException(String message) {
        super(message);
    }
}
