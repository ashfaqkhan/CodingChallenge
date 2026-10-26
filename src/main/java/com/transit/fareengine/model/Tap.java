package com.transit.fareengine.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a tap event in the transit fare engine system.
 * This class contains information about a tap, including its ID, timestamp, type, stop ID, company ID, bus ID, and PAN (Primary Account Number).
 */
@Data
@NoArgsConstructor   // Generates a no-argument constructor
@AllArgsConstructor
public class Tap {
    private Long id;

    private LocalDateTime dateTimeUtc;

    private TapType tapType;

    private String stopId;

    private String companyId;

    private String busId;

    private String pan;
}
