package com.transit.fareengine.service;

import com.transit.fareengine.exception.FareEngineException;
import com.transit.fareengine.model.TripFare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FareCalculationService.class);

    private final Map<String, TripFare> fareMatrix;
    private static final String FARES_CONFIG = "/fares.csv";

    /**
     * Loads the fare matrix from resources/fares.csv.
     *
     * <p>Throws a {@code FareEngineException} if the fare configuration
     * cannot be loaded.
     */
    public FareCalculationService() {
            this.fareMatrix = loadFareMatrix();
    }

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
        final TripFare tripFare =
                fareMatrix.get(
                        buildRouteKey(fromStopId, toStopId));

        if (tripFare == null) {
            throw new FareEngineException(
                    String.format(
                            "Fare not configured for route [%s -> %s]",
                            fromStopId,
                            toStopId));
        }

        return tripFare.getFare();

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

        return fareMatrix.values()
                .stream()
                .filter(fare ->
                        fare.getFromStopId().equals(fromStopId)
                                || fare.getToStopId().equals(fromStopId))
                .map(TripFare::getFare)
                .max(BigDecimal::compareTo)
                .orElseThrow(() ->
                        new FareEngineException(
                                String.format(
                                        "No fare configuration found for stop [%s]",
                                        fromStopId)));
    }

    /**
     * Loads the dynamic fare configuration from resources/fares.csv by reading
     * the file and parsing each line into a TripFare object.
     * @return map of TripFare objects representing the fare matrix
     */
    private Map<String, TripFare> loadFareMatrix() {

        Map<String, TripFare> fares = new HashMap<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     Objects.requireNonNull(
                                             getClass()
                                                     .getResourceAsStream(
                                                             FARES_CONFIG))))) {

            reader.readLine(); // skip header

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");

                String fromStop = columns[0].trim();
                String toStop = columns[1].trim();

                TripFare tripFare = new TripFare(
                        fromStop,
                        toStop,
                        new BigDecimal(columns[2].trim()));

                fares.put(
                        buildRouteKey(fromStop, toStop),
                        tripFare);
            }

        } catch (IOException ex) {

            throw new FareEngineException(
                    "Unable to load fare matrix"
            );
        }

        return Map.copyOf(fares);
    }

    private static String buildRouteKey(String fromStopId,
                                        String toStopId) {
        return Stream.of(fromStopId, toStopId)
                .sorted()
                .collect(Collectors.joining("-"));
    }

}

