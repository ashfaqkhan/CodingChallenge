package com.transit.fareengine.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripFare {

    private String fromStopId;

    private String toStopId;

    private BigDecimal fare;

}
