package org.zerock.triplet.domain.mytrip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageItem {
    private Long usageId;
    private Integer categoryId;
    private String categoryName;
    private Long amount;                 // usage_cost
    private String memo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime costDateTime;  // 사용 시각
}
