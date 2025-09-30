package org.zerock.triplet.domain.mycard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MycardListItemDTO {
    private Long mcardId;

    private Long cardId;

    private String cardName;

    private String cardNickname;

    private Integer cardStatus;

    private String cardNum;

    private String account;

    private boolean checkGather;
}
