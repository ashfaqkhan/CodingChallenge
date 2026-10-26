package com.transit.fareengine.integration;

import com.transit.fareengine.FareEngineApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@code CsvTapProcessor}.
 *
 * <p>These tests validate the end-to-end processing of CSV tap data
 * into trip records.
 */
class CsvTapProcessorIT {

    @TempDir
    Path temporaryDirectory;

    @Test
    void processesCsvTapsIntoCompletedCancelledAndIncompleteTrips() throws IOException {
        Path input = temporaryDirectory.resolve("taps.csv");
        Path output = temporaryDirectory.resolve("trips.csv");

        Files.write(input, List.of(
                "ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN",
                "1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,pan-completed",
                "2,22-01-2023 13:05:00,OFF,Stop2,Company1,Bus37,pan-completed",
                "3,22-01-2023 14:00:00,ON,Stop1,Company1,Bus37,pan-cancelled",
                "4,22-01-2023 14:02:00,OFF,Stop1,Company1,Bus37,pan-cancelled",
                "5,22-01-2023 15:00:00,ON,Stop3,Company1,Bus36,pan-incomplete"));

        FareEngineApplication.main(new String[]{input.toString(), output.toString()});

        assertEquals(List.of(
                "Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status",
                "22-01-2023 13:00:00,22-01-2023 13:05:00,300,Stop1,Stop2,3.25,Company1,Bus37,pan-completed,COMPLETED",
                "22-01-2023 14:00:00,22-01-2023 14:02:00,120,Stop1,Stop1,0.00,Company1,Bus37,pan-cancelled,CANCELLED",
                "22-01-2023 15:00:00,,0,Stop3,,7.30,Company1,Bus36,pan-incomplete,INCOMPLETE"),
                Files.readAllLines(output));
    }
}
