package org.zerock.triplet.domain.mytrip.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripUpdateReq {
    private String title;
    private Integer theme;
    private LocalDate startDate;
    private LocalDate  endDate;
    private String coverKey;
    private List<BudgetUpdateReq> budgets;
    private List<CostPlan> costPlans;
}
