package org.zerock.triplet.domain.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CardWithBenefitsDTO {
    private Long cardId;
    private String cardName;
    private String cardIntro;
    private String cardDesc;
    private List<BenefitDTO> benefits;
}
