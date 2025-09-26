package org.zerock.triplet.domain.mytrip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostPlan {
    private Integer day;
    private Long  food;
    private Long  transport;
    private Long  leisure;
    private Long  etc;
    private Boolean checkPlan;
}
