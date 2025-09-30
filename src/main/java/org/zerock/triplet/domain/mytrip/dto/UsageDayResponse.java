package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageDayResponse {
    private Long tripId;
    private UsageDay day;
}
