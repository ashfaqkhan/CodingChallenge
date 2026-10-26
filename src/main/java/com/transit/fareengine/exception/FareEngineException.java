package com.transit.fareengine.exception;

/**
 * Exception thrown when there is an issue with fare calculation or fare configuration.
 */
public class FareEngineException extends RuntimeException {
    public FareEngineException(String message) {
        super(message);
    }
}
