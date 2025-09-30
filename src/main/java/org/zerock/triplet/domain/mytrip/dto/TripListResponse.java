package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripListResponse {
    private List<TripListItem> trips;
}
