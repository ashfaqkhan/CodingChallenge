package com.transit.fareengine.service;

import com.transit.fareengine.exception.InvalidTapException;
import com.transit.fareengine.model.Tap;
import com.transit.fareengine.model.TapType;
import com.transit.fareengine.model.Trip;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Orchestrates streaming processing of transit tap data.
 *
 * <h2>Processing Strategy</h2>
 * <ul>
 *     <li>Input file is processed sequentially.</li>
 *     <li>The entire file is never loaded into memory.</li>
 *     <li>Only active trips are retained in memory.</li>
 *     <li>Generated trips are immediately written to the output file.</li>
 * </ul>
 *
 * <h2>Input CSV Format</h2>
 *
 * <pre>
 * ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN
 * 1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559
 * 2,22-01-2023 13:05:00,OFF,Stop2,Company1,Bus37,5500005555555559
 * </pre>
 *
 * <h2>Output CSV Format</h2>
 *
 * <pre>
 * Started,Finished,DurationSecs,FromStopId,ToStopId,
 * ChargeAmount,CompanyId,BusID,PAN,Status
 * </pre>
 *
 * <h2>Error Handling</h2>
 *
 * Invalid records are delegated to TripProcessingService for validation.
 * Processing exceptions are surfaced to the caller.
 *
 * <p>The implementation is intentionally stream-oriented to minimize
 * memory consumption. Only active trips are retained in memory while
 * processing large input files.
 */
@NoArgsConstructor
@AllArgsConstructor
public class CsvTapProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CsvTapProcessor.class);

    private TripProcessingService tripProcessingService;

    private static final DateTimeFormatter
            DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Processes an input tap file and generates the corresponding trip file.
     *
     * @param inputFile  path to the input taps CSV file
     * @param outputFile path to the generated trips CSV file
     */
    public void process(Path inputFile,
                        Path outputFile) {

        try (BufferedReader reader =
                     Files.newBufferedReader(inputFile);

             BufferedWriter writer =
                     Files.newBufferedWriter(outputFile)) {

            writeHeader(writer);

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                try {

                    Tap tap = parseTap(line);

                    Optional<Trip> trip =
                            tripProcessingService.processTap(tap);

                    if (trip.isPresent()) {
                        writeTrip(writer, trip.get());
                    }

                } catch (InvalidTapException ex) {
                    LOGGER.warn(
                            "Ignoring invalid tap record [{}]. Reason [{}]",
                            line,
                            ex.getMessage());

                } catch (Exception ex) {

                    LOGGER.error(
                            "Unexpected error processing record [{}]",
                            line,
                            ex);

                }
            }

            processIncompleteTrips(writer);

        } catch (IOException ex) {

            throw new RuntimeException(
                    String.format(
                            "Failed to process input [%s] output [%s]",
                            inputFile,
                            outputFile),
                    ex);

        } finally {
            LOGGER.info(
                    "Completed processing input [{}] output [{}]",
                    inputFile,
                    outputFile);
        }
    }

    /**
     * Writes the CSV header to the output file.
     *
     * @param writer BufferedWriter for the output file
     * @throws IOException if an I/O error occurs
     */
    private void writeHeader(BufferedWriter writer)
            throws IOException {

        writer.write(
                "Started,Finished,DurationSecs,FromStopId,ToStopId,"
                        + "ChargeAmount,CompanyId,BusID,PAN,Status");
        writer.newLine();
    }

    /**
     * Writes a single Trip to the output CSV file.
     *
     * @param writer BufferedWriter for the output file
     * @param trip   Trip object to write
     * @throws IOException if an I/O error occurs
     */
    private void writeTrip(BufferedWriter writer,
                           Trip trip)
            throws IOException {
        writer.write(tripToCsvLine(trip));
        writer.newLine();
    }

    /**
     * Converts a Trip object to a CSV line.
     *
     * @param trip Trip object to convert
     * @return CSV line representing the trip
     */
    private String tripToCsvLine(Trip trip) {
        return String.format(
                Locale.ROOT,
                "%s,%s,%d,%s,%s,%.2f,%s,%s,%s,%s",
                formatDateTime(trip.getStarted()),
                formatDateTime(trip.getFinished()),
                trip.getDurationSecs(),
                trip.getFromStopId(),
                trip.getToStopId() == null ? "" : trip.getToStopId(),
                trip.getChargeAmount(),
                trip.getCompanyId(),
                trip.getBusId(),
                trip.getPan(),
                trip.getStatus());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * Parses a single line of the input CSV file into a Tap domain object.
     *
     * @param line single line from the input CSV file
     * @return parsed Tap object
     */
    private Tap parseTap(String line) {

        String[] columns = line.split(",");

        Tap tap = new Tap();

        tap.setId(Long.parseLong(columns[0].trim()));
        tap.setDateTimeUtc(
                LocalDateTime.parse(
                        columns[1].trim(),
                        DATE_TIME_FORMATTER));

        tap.setTapType(
                TapType.valueOf(columns[2].trim()));

        tap.setStopId(columns[3].trim());
        tap.setCompanyId(columns[4].trim());
        tap.setBusId(columns[5].trim());
        tap.setPan(columns[6].trim());

        return tap;
    }

    /**
     * Finalizes any remaining active trips and writes them to the output file.
     *
     * @param writer BufferedWriter for the output file
     * @throws IOException if an I/O error occurs
     */
    private void processIncompleteTrips(
            BufferedWriter writer) throws IOException {

        List<Trip> remainingTrips =
                tripProcessingService.processRemainingTrips();

        for (Trip trip : remainingTrips) {

            writeTrip(writer, trip);
        }
    }
}
