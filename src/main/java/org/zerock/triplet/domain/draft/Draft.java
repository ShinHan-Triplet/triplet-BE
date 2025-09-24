package org.zerock.triplet.domain.draft;

import lombok.Data;

import java.util.Map;

@Data
public class Draft {
    private String title;
    private Integer themeNum;
    private Long startMs;
    private Long endMs;
    private Cover cover;
    private Budgets budgets;
    private Long updatedAt;
    private String gatherName;

    @Data public static class Cover{
        private String fileName;
        private String objectKey; //s3 tmp 키
        private String contentType;
        private Long size;
        private String viewUrl;
    }

    @Data public static class Budgets{
        private String stay;
        private String insurance;
        private Map<Integer, Day> days; // 1: Day~
    }

    @Data public static class Day{
        private Boolean noSchedule;
        private Map<String, String> amounts; //food,transport,leisure,etc
    }
}
