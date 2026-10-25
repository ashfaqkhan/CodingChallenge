package com.transit.fareengine.model;

import lombok.*;

import java.time.LocalDateTime;

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
