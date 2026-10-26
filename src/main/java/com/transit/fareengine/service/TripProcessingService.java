package com.transit.fareengine.service;

import com.transit.fareengine.exception.InvalidTapException;
import com.transit.fareengine.model.Tap;
import com.transit.fareengine.model.Trip;
import com.transit.fareengine.model.TripStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.temporal.ChronoUnit;

/**
 * Handles the trip lifecycle and tap matching logic.
 *
 * <p>Responsibilities include:
 * <ul>
 *     <li>Tracking active trips by PAN.</li>
 *     <li>Matching Tap ON and Tap OFF events.</li>
 *     <li>Determining trip status.</li>
 *     <li>Creating Trip domain objects.</li>
 *     <li>Delegating fare calculations to {@code FareCalculationService}.</li>
 * </ul>
 *
 * <p>A HashMap keyed by PAN is used to provide O(1) lookup for
 * active journeys.
 */
@NoArgsConstructor
@AllArgsConstructor
public class TripProcessingService {

    private FareCalculationService fareCalculationService;

    private final Map<String, Tap> activeTrips =
            new HashMap<>();

    /**
     * Processes an incoming tap event.
     *
     * <p>Depending on the tap type and currently active trips,
     * this method may:
     * <ul>
     *     <li>Start a new active trip.</li>
     *     <li>Complete an existing trip.</li>
     *     <li>Create a cancelled trip.</li>
     *     <li>Reject invalid tap sequences.</li>
     * </ul>
     *
     * @param tap incoming tap event
     * @return completed trip if one is produced, otherwise empty
     */
    public Optional<Trip> processTap(Tap tap){
        return switch (tap.getTapType()) {
            case ON -> processTapOn(tap);
            case OFF -> processTapOff(tap);
        };
    }

   /**
     * processTapOff handles the logic for processing a Tap OFF event.
     *
     * <p>It attempts to match the Tap OFF with an existing Tap ON
     * based on PAN. If a match is found, it determines whether the
     * trip is completed or canceled and creates the appropriate
     * Trip object.
     *
     * @param tapOff incoming Tap OFF event
     * @return completed or canceled trip if one is produced, otherwise empty
     */
    private Optional<Trip> processTapOff(Tap tapOff) {
        Tap tapOn =
                activeTrips.remove(tapOff.getPan());

        if (tapOn == null) {
            throw new InvalidTapException(
                    "OFF tap received without active journey");
        }

        if (tapOn.getStopId()
                .equals(tapOff.getStopId())) {

            return Optional.of(
                    createCancelledTrip(
                            tapOn,
                            tapOff));
        }

        return Optional.of(
                createCompletedTrip(
                        tapOn,
                        tapOff));
    }

    /**
     * Creates a COMPLETED trip based on matching Tap ON and Tap OFF events.
     *
     * @param tapOn the Tap ON event
     * @param tapOff the Tap OFF event
     * @return a COMPLETED Trip object
     */
    private Trip createCompletedTrip(Tap tapOn, Tap tapOff) {
     Trip trip = new Trip();
     trip.setStarted(tapOn.getDateTimeUtc());
     trip.setFinished(tapOff.getDateTimeUtc());
     trip.setDurationSecs(ChronoUnit.SECONDS.between(tapOn.getDateTimeUtc(), tapOff.getDateTimeUtc()));
     trip.setFromStopId(tapOn.getStopId());
     trip.setToStopId(tapOff.getStopId());
     trip.setChargeAmount(fareCalculationService.calculateFare(tapOn.getStopId(), tapOff.getStopId()));
     trip.setCompanyId(tapOn.getCompanyId());
     trip.setBusId(tapOn.getBusId());
     trip.setPan(tapOn.getPan());
     trip.setStatus(TripStatus.COMPLETED);
     return trip;
    }

    /**
     * Creates a CANCELLED trip based on matching Tap ON and Tap OFF events
     * at the same stop.
     *
     * @param tapOn the Tap ON event
     * @param tapOff the Tap OFF event
     * @return a CANCELLED Trip object
     */
    private Trip createCancelledTrip(Tap tapOn, Tap tapOff) {
        Trip trip = new Trip();
        trip.setStarted(tapOn.getDateTimeUtc());
        trip.setFinished(tapOff.getDateTimeUtc());
        trip.setDurationSecs(ChronoUnit.SECONDS.between(tapOn.getDateTimeUtc(), tapOff.getDateTimeUtc()));
        trip.setFromStopId(tapOn.getStopId());
        trip.setToStopId(tapOff.getStopId());
        trip.setChargeAmount(fareCalculationService.calculateFare(tapOn.getStopId(), tapOff.getStopId()));
        trip.setCompanyId(tapOn.getCompanyId());
        trip.setBusId(tapOn.getBusId());
        trip.setPan(tapOn.getPan());
        trip.setStatus(TripStatus.CANCELLED);
        return trip;
    }

    /**
     * Creates an INCOMPLETE trip based on an existing Tap ON event.
     * @param existingTap the unmatched Tap ON event
     * @return an INCOMPLETE trip
     */
    private Trip createIncompleteTrip(Tap existingTap) {
        Trip trip = new Trip();
        trip.setStarted(existingTap.getDateTimeUtc());
        trip.setFromStopId(existingTap.getStopId());
        trip.setChargeAmount(fareCalculationService.calculateFare(existingTap.getStopId(), existingTap.getStopId()));
        trip.setCompanyId(existingTap.getCompanyId());
        trip.setBusId(existingTap.getBusId());
        trip.setPan(existingTap.getPan());
        trip.setStatus(TripStatus.INCOMPLETE);
        return trip;
    }

    /**
     * processTapOn handles the logic for processing a Tap ON event.
     *
     * <p>It checks if there is an existing active trip for the same PAN.
     * If there is no existing trip, it adds the Tap ON to the active trips.
     * If there is an existing trip, it creates an INCOMPLETE trip based on
     * the existing Tap ON and returns it.
     *
     * @param tap incoming Tap ON event
     * @return incomplete trip if one is produced, otherwise empty
     */
    private Optional<Trip> processTapOn(Tap tap) {
        Tap existingTap =
                activeTrips.put(tap.getPan(), tap);

        if (existingTap == null) {
            return Optional.empty();
        }

        return Optional.of(
                createIncompleteTrip(existingTap)
        );
    }

    /**
     * Finalizes all remaining active trips when end-of-file is reached.
     *
     * <p>Trips that have an unmatched Tap ON are classified as
     * INCOMPLETE and charged the maximum possible fare from
     * their originating stop.
     *
     * @return list of incomplete trips
     */
    public List<Trip> processRemainingTrips() {
        List<Trip> incompleteTrips = activeTrips.values().stream()
                .map(this::createIncompleteTrip)
                .toList();
        activeTrips.clear();
        return incompleteTrips;
    }
}
