package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetTotal {
    private Long food;
    private Long transport;
    private Long leisure;
    private Long etc;
    private Long total;
}
