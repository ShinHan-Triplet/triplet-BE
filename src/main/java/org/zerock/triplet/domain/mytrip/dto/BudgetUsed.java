package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUsed {
    private Integer categoryId;
    private Long used;
}
