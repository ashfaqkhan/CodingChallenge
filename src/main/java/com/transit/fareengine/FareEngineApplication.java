package com.transit.fareengine;

import com.transit.fareengine.service.CsvTapProcessor;
import com.transit.fareengine.service.FareCalculationService;
import com.transit.fareengine.service.TripProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;

/**
 * Entry point for the Fare Engine application.
 */
public final class FareEngineApplication {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FareEngineApplication.class);

    /**
     * Entry point for the Fare Engine application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        LOGGER.info("Fare Engine started");
        validateArguments(args);
        LOGGER.info("Input file : {}", args[0]);
        LOGGER.info("Output file: {}", args[1]);

        FareCalculationService fareCalculationService =
                new FareCalculationService();

        TripProcessingService tripProcessingService =
                new TripProcessingService(fareCalculationService);

        CsvTapProcessor processor =
                new CsvTapProcessor(tripProcessingService);

        processor.process(
                Path.of(args[0]),
                Path.of(args[1]));
        LOGGER.info("Fare Engine Completed");
    }

    /**
     * Validates the command line arguments.
     *
     * <p>Expects exactly two arguments: input file and output file.
     *
     * @param args command line arguments
     * @throws IllegalArgumentException if the number of arguments is not two
     */
    private static void validateArguments(final String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Expected arguments: <input-file> <output-file>");
        }
    }
}
