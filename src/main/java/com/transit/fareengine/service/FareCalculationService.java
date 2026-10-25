package com.transit.fareengine.service;

import java.math.BigDecimal;

/**
 * Encapsulates fare calculation rules.
 *
 * <p>This service is responsible for:
 * <ul>
 *     <li>Determining fares for completed trips.</li>
 *     <li>Calculating maximum fares for incomplete trips.</li>
 *     <li>Managing the static fare matrix defined by the challenge.</li>
 * </ul>
 *
 * <p>No trip matching or CSV processing logic should exist in this class.
 */
public class FareCalculationService {

    /**
     * Determines the charge amount for a completed trip.
     *
     * @param fromStopId origin stop
     * @param toStopId destination stop
     * @return applicable fare
     */
    public BigDecimal calculateFare(
            String fromStopId,
            String toStopId) {
        return BigDecimal.ZERO;

    }

    /**
     * Determines the maximum possible fare from a given stop.
     *
     * <p>Used when a trip is classified as INCOMPLETE.
     *
     * @param fromStopId origin stop
     * @return maximum fare from the supplied stop
     */
    public BigDecimal calculateMaxFare(
            String fromStopId) {
        return BigDecimal.ZERO;
    }

}

