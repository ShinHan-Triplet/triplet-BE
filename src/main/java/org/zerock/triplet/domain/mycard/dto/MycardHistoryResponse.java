package org.zerock.triplet.domain.mycard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MycardHistoryResponse {
    private Long mcardId;

    private Long cardId;

    private String cardName;

    private String cardNickname;

    private String cardNum;

    private String account;

    private List<UsageItemDTO> histories;

    public MycardHistoryResponse(Long mcardId, Long cardId, String name,
                                    String nickname,
                                    String cardNumber, String linkedAccount) {
        this.mcardId = mcardId;
        this.cardId = cardId;
        this.cardName = name;
        this.cardNickname = nickname;
        this.cardNum = cardNumber;
        this.account = linkedAccount;
    }
}
