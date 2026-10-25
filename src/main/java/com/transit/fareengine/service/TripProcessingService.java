package com.transit.fareengine.service;

import com.transit.fareengine.model.Tap;
import com.transit.fareengine.model.Trip;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

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
        return Optional.empty();
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
        return List.of();
    }
}
