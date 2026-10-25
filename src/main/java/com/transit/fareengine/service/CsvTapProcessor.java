package com.transit.fareengine.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

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

    private TripProcessingService tripProcessingService;

    /**
     * Processes an input tap file and generates the corresponding trip file.
     *
     * @param inputFile path to the input taps CSV file
     * @param outputFile path to the generated trips CSV file
     */
    public void process(
            Path inputFile, Path outputFile) {
    }

}
