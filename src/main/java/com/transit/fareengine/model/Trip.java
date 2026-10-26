package com.transit.fareengine.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a trip in the transit fare engine system.
 * This class contains information about a trip, including its start and end times, duration, stops, charge amount, company ID, bus ID, PAN (Primary Account Number), and status.
 */
@Data
public class Trip {

    private LocalDateTime started;

    private LocalDateTime finished;

    private long durationSecs;

    private String fromStopId;

    private String toStopId;

    private BigDecimal chargeAmount;

    private String companyId;

    private String busId;

    private String pan;

    private TripStatus status;
}
