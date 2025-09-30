package org.zerock.triplet.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class MemberIdsByGatherDTO {

    private final Long gatherId;
    private final Long memberId;
}
