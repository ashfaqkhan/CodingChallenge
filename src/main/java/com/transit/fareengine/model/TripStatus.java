package com.transit.fareengine.model;

/**
 * Enum representing the status of a trip in the transit fare engine system.
 * It can either be "COMPLETED" (indicating a successful trip), "INCOMPLETE" (indicating an unfinished trip), or "CANCELLED" (indicating a trip that was cancelled).
 */
public enum TripStatus {
    COMPLETED, INCOMPLETE, CANCELLED
}
