package org.zerock.triplet.domain.gather.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.gather.dto.*;
import org.zerock.triplet.domain.gather.entity.Gather;
import org.zerock.triplet.domain.gather.repository.GatherMappingRepository;
import org.zerock.triplet.domain.gather.repository.GatherRepository;
import org.zerock.triplet.domain.member.dto.MemberIdsByGatherDTO;
import org.zerock.triplet.domain.card.repository.MemberCardRepository;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.domain.member.dto.IdName;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatherService {

    private final GatherRepository gatherRepo;
    private final GatherMappingRepository gmRepo;
    private final MemberRepository memberRepo;
    private final MemberCardRepository cardRepo;

    @Transactional(readOnly = true)
    public GatherResponse getMyOptions(Long memberId){

        // 내가 방장인 모임들, 솔로 모임 제와
        List<Long> ownerGatherIds = gmRepo.findOwnedGatherIds(memberId);
        List<GatherDTO> owner = mapGathersWithMembers(ownerGatherIds);

        // 나만 있는 모임들 (gather의 인원수가 1명이고, 그 1명이 나인 경우)
        List<Long> soloGatherIds = gmRepo.findSoloGatherIds(memberId);
        List<GatherDTO> solo = mapSolosWithMembers(soloGatherIds);
        return new GatherResponse(owner, solo);
    }

    public MyCardListResponse getMyCards(Long memberId){
        // 개인카드
        List<MemberCardDTO> cards = cardRepo.findPersonalCards(memberId).stream()
                .map(c -> MemberCardDTO.builder()
                        .mcardId(c.getMcardId())
                        .nickname(c.getNickname())
                        .maskedNumber(c.getMaskedNumber())
                        .cardId(c.getCardId())
                        .build())
                .toList();
        return new MyCardListResponse(cards);
    }


    private List<GatherDTO> mapGathersWithMembers(List<Long> gatherIds){
        if(gatherIds.isEmpty()) return List.of();

        // 모임 기본정보
        Map<Long, Gather> gatherMap = gatherRepo.findAllById(gatherIds).stream()
                .collect(Collectors.toMap(Gather::getId, g->g));

        // 모임별 멤버 id 리스트
        Map<Long, List<Long>> membersByGather = gmRepo.findMemberIdsByGatherIds(gatherIds).stream()
                .collect(Collectors.groupingBy(
                        MemberIdsByGatherDTO::getGatherId,
                        Collectors.mapping(MemberIdsByGatherDTO::getMemberId, Collectors.toList())));

        // 모든 멤버 이름 조회 후 매핑
        Set<Long> allMemberIds = membersByGather.values().stream()
                .flatMap(List::stream).collect(Collectors.toSet());
        Map<Long, String> nameById = memberRepo.findNamesByIds(allMemberIds).stream()
                .collect(Collectors.toMap(IdName::getId, IdName::getName, (a,b) -> a));

        return gatherIds.stream().map(gid -> {
            Gather g = gatherMap.get(gid);
            List<Long> mids = membersByGather.getOrDefault(gid, List.of());
            List<String> names = mids.stream().map(nameById::get).toList();
            return GatherDTO.builder()
                    .gatherId(gid)
                    .title(g.getName())
                    .memberCount(mids.size())
                    .members(names)
                    .cardId(g.getMcard().getCard().getId())
                    .build();
        }).toList();
    }

    private List<GatherDTO> mapSolosWithMembers(List<Long> gatherIds){
        if(gatherIds.isEmpty()) return List.of();

        // 모임 기본정보
        Map<Long, Gather> gatherMap = gatherRepo.findAllById(gatherIds).stream()
                .collect(Collectors.toMap(Gather::getId, g->g));

        // 모임별 멤버 id 리스트
        Map<Long, List<Long>> membersByGather = gmRepo.findMemberIdsByGatherIds(gatherIds).stream()
                .collect(Collectors.groupingBy(
                        MemberIdsByGatherDTO::getGatherId,
                        Collectors.mapping(MemberIdsByGatherDTO::getMemberId, Collectors.toList())));

        // 모든 멤버 이름 조회 후 매핑
        Set<Long> allMemberIds = membersByGather.values().stream()
                .flatMap(List::stream).collect(Collectors.toSet());
        Map<Long, String> nameById = memberRepo.findNamesByIds(allMemberIds).stream()
                .collect(Collectors.toMap(IdName::getId, IdName::getName, (a,b) -> a));

        return gatherIds.stream().map(gid -> {
            Gather g = gatherMap.get(gid);
            List<Long> mids = membersByGather.getOrDefault(gid, List.of());
            List<String> names = mids.stream().map(nameById::get).toList();
            return GatherDTO.builder()
                    .gatherId(gid)
                    .title(g.getName())
                    .memberCount(mids.size())
                    .members(names)
                    .cardId(g.getMcard().getCard().getId())
                    .build();
        }).toList();
    }
}
