package org.zerock.triplet.domain.trip.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CreateTripRequest {
    private String title;
    private Integer theme;
    private Long startMs;
    private Long endMs;
    private Long gatherId;

    // budgets: { stay: "230000", insurance: "30000" } 같은 문자열일 수 있어 숫자로 파싱
    private Budgets budgets;

    // days: { "1": { noSchedule:false, amounts:{food:"140000", transport:"32000", ... } }, "2": {...} }
    private Map<Integer, DayPlan> days;

    private String presetImage;

    @Data
    public static class Budgets{
        private String stay;
        private String insurance;
    }

    @Data
    public static class DayPlan{
        private Boolean noSchedule;
        private Amounts amounts;
    }

    @Data
    public static class Amounts{
        private String food;
        private String transport;
        private String leisure;
        private String etc;
    }
}
