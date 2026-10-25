package com.transit.fareengine.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
