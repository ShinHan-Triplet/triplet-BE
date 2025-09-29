package org.zerock.triplet.domain.mycard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsageUpdateRequest {
    private String memo;
    private Integer category;
}
