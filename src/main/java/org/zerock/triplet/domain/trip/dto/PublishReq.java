package org.zerock.triplet.domain.trip.dto;

import lombok.Data;

@Data
public class PublishReq {
    private Long gatherId;
    private NewGather newGather;
}
