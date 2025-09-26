package org.zerock.triplet.domain.card.dto;

import lombok.Data;

@Data
public class CardReqDTO {
    private Long cardId;
    private String name;
    private String phone;
    private String memberNum;
    private String address;
    private String account;
    private String pw;
    private String nickName;
    private boolean checkGather;
}
