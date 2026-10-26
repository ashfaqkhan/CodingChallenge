package com.transit.fareengine.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * Represents the fare for a trip in the transit fare engine system.
 * This class contains information about the fare, including the starting and ending stop IDs and the fare amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripFare {

    private String fromStopId;

    private String toStopId;

    private BigDecimal fare;

}
