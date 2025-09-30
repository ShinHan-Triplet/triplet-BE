package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMember {
    private Long memberId;
    private String name;
    private Boolean checkOwner;
}
