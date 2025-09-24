package org.zerock.triplet.domain.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublishResult {
    private Long tripId;
    private Long gatherId;
}
