package org.zerock.triplet.domain.gather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyCardListResponse {
    private List<MemberCardDTO> personalCards;
}
