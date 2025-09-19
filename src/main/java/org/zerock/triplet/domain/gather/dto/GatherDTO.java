package org.zerock.triplet.domain.gather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatherDTO {
    private Long gatherId;
    private String title;
    private Integer memberCount;
    private List<String> members;
    private Long cardId;
}
