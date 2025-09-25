package org.zerock.triplet.domain.mytrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCard {
    private Long mcardId;
    private Long cardId;
    private String cardName;
    private String cardNickname;
    private String cardNum;
    private String account;
    private Integer status;
}
