package org.zerock.triplet.domain.mytrip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDetailResponse {
    private Long tripId;
    private String title;
    private String gatherName;
    private List<TripMember> members;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;
    private String theme;          // 필요 시 enum/코드로 교체 가능
    private String thumbnail;
    private String status;
    private TripCard card;         // 연결 카드 정보
    private List<BudgetItem> budget;
    private Long total;
}
