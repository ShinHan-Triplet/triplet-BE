package org.zerock.triplet.domain.mytrip.dto;

import lombok.Data;

@Data
public class BudgetUpdateReq {
    private String category;
    private Long amount;
}
