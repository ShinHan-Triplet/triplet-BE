package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetItem {
    private Integer categoryId;
    private String categoryName;
    private Long planned;
    private Long used;
}