package com.transit.fareengine.service;

import com.transit.fareengine.exception.InvalidTapException;
import com.transit.fareengine.model.Tap;
import com.transit.fareengine.model.TapType;
import com.transit.fareengine.model.Trip;
import com.transit.fareengine.model.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@code TripProcessingService}.
 *
 * <p>These tests validate the tap matching and trip lifecycle logic.
 */
class TripProcessingServiceTest {

    private static final LocalDateTime START =
            LocalDateTime.of(2023, 1, 22, 13, 0);
    private TripProcessingService tripProcessingService;

    @BeforeEach
    void setUp() {
        tripProcessingService =
                new TripProcessingService(new FareCalculationService());
    }

    @Test
    void matchesTapOnAndTapOffIntoCompletedTrip() {
        assertFalse(tripProcessingService.processTap(tap(1, START, TapType.ON,
                "Stop1", "pan-1")).isPresent());

        Optional<Trip> result = tripProcessingService.processTap(
                tap(2, START.plusMinutes(5), TapType.OFF, "Stop2", "pan-1"));

        assertTrue(result.isPresent());
        Trip trip = result.orElseThrow();
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertEquals(START, trip.getStarted());
        assertEquals(START.plusMinutes(5), trip.getFinished());
        assertEquals(300, trip.getDurationSecs());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals("Stop2", trip.getToStopId());
        assertEquals(new BigDecimal("3.25"), trip.getChargeAmount());
        assertEquals("Company1", trip.getCompanyId());
        assertEquals("Bus37", trip.getBusId());
        assertEquals("pan-1", trip.getPan());
    }

    @Test
    void createsZeroFareCancelledTripWhenTapsUseSameStop() {
        tripProcessingService.processTap(tap(1, START, TapType.ON,
                "Stop1", "pan-1"));

        Trip trip = tripProcessingService.processTap(tap(2, START.plusMinutes(2),
                TapType.OFF, "Stop1", "pan-1")).orElseThrow();

        assertEquals(TripStatus.CANCELLED, trip.getStatus());
        assertEquals(new BigDecimal("0"), trip.getChargeAmount());
        assertEquals(120, trip.getDurationSecs());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals("Stop1", trip.getToStopId());
    }

    @Test
    void rejectsTapOffWithoutAnActiveTrip() {
        assertThrows(InvalidTapException.class,
                () -> tripProcessingService.processTap(
                        tap(1, START, TapType.OFF, "Stop2", "pan-1")));
    }

    @Test
    void finalizesUnmatchedTapOnAtMaximumFareAndClearsIt() {
        tripProcessingService.processTap(tap(1, START, TapType.ON,
                "Stop1", "pan-1"));

        List<Trip> incompleteTrips =
                tripProcessingService.processRemainingTrips();

        assertEquals(1, incompleteTrips.size());
        Trip trip = incompleteTrips.get(0);
        assertEquals(TripStatus.INCOMPLETE, trip.getStatus());
        assertEquals(new BigDecimal("7.30"), trip.getChargeAmount());
        assertEquals(START, trip.getStarted());
        assertEquals(0, trip.getDurationSecs());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals(null, trip.getFinished());
        assertEquals(null, trip.getToStopId());
        assertTrue(tripProcessingService.processRemainingTrips().isEmpty());
    }

    @Test
    void replacesAnExistingTapOnAndReturnsItAsIncomplete() {
        tripProcessingService.processTap(tap(1, START, TapType.ON,
                "Stop1", "pan-1"));

        Trip incomplete = tripProcessingService.processTap(tap(2,
                START.plusMinutes(10), TapType.ON, "Stop2", "pan-1"))
                .orElseThrow();

        assertEquals(TripStatus.INCOMPLETE, incomplete.getStatus());
        assertEquals(new BigDecimal("7.30"), incomplete.getChargeAmount());

        Trip completed = tripProcessingService.processTap(tap(3,
                START.plusMinutes(15), TapType.OFF, "Stop3", "pan-1"))
                .orElseThrow();
        assertEquals(TripStatus.COMPLETED, completed.getStatus());
        assertEquals(new BigDecimal("5.50"), completed.getChargeAmount());
    }

    private Tap tap(long id, LocalDateTime time, TapType type, String stop, String pan) {
        return new Tap(id, time, type, stop, "Company1", "Bus37", pan);
    }
}