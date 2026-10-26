package com.transit.fareengine.service;

import com.transit.fareengine.exception.FareEngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@code FareCalculationService}.
 *
 * <p>These tests validate the fare calculation logic and
 * fare matrix configuration.
 */
class FareCalculationServiceTest {

    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        fareCalculationService = new FareCalculationService();
    }

    @Test
    void calculatesConfiguredFareInBothDirections() {
        assertEquals(new BigDecimal("3.25"),
                fareCalculationService.calculateFare("Stop1", "Stop2"));
        assertEquals(new BigDecimal("3.25"),
                fareCalculationService.calculateFare("Stop2", "Stop1"));
        assertEquals(new BigDecimal("5.50"),
                fareCalculationService.calculateFare("Stop2", "Stop3"));
        assertEquals(new BigDecimal("5.50"),
                fareCalculationService.calculateFare("Stop3", "Stop2"));
        assertEquals(new BigDecimal("7.30"),
                fareCalculationService.calculateFare("Stop1", "Stop3"));
        assertEquals(new BigDecimal("7.30"),
                fareCalculationService.calculateFare("Stop3", "Stop1"));
    }

    @Test
    void calculatesMaximumFareFromEachStop() {
        assertEquals(new BigDecimal("7.30"),
                fareCalculationService.calculateMaxFare("Stop1"));
        assertEquals(new BigDecimal("5.50"),
                fareCalculationService.calculateMaxFare("Stop2"));
        assertEquals(new BigDecimal("7.30"),
                fareCalculationService.calculateMaxFare("Stop3"));
    }

    @Test
    void throwsWhenRouteHasNoConfiguredFare() {
        assertThrows(FareEngineException.class,
                () -> fareCalculationService.calculateFare("Stop1", "Unknown"));
    }

    @Test
    void throwsWhenStopHasNoConfiguredFare() {
        assertThrows(FareEngineException.class,
                () -> fareCalculationService.calculateMaxFare("Unknown"));
    }
}