package org.zerock.triplet.domain.mycard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitDTO {
    private String benefitTitle;

    private String benefitContent;
}
