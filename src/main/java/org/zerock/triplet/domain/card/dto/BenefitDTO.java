package org.zerock.triplet.domain.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BenefitDTO {
    private String title;
    private String content;
    private String shortTitle;
    private String shortContent;
}
