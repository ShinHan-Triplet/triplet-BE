package org.zerock.triplet.domain.mycard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MycardDetailDTO {
    private Long mcardId;

    private Long cardId;

    private String name;

    private String nickname;

    private Integer cardStatus;

    private String maskedNumber;

    private String linkedAccount;

    private boolean checkGather;

    private List<BenefitDTO> benefits;
}
