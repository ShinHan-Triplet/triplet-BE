package org.zerock.triplet.domain.gather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCardDTO {
    private Long mcardId;
    private String nickname;
    private String maskedNumber;
    private Long cardId;
}
