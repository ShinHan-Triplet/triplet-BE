package org.zerock.triplet.domain.gather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GatherResponse {
    private List<GatherDTO> ownerGathers; //내가 방장
    private List<GatherDTO> soloGathers; //나만의 모임
}
