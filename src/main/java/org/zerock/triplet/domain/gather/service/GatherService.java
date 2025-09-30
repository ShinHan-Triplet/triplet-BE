package org.zerock.triplet.domain.gather.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.gather.dto.*;
import org.zerock.triplet.domain.gather.entity.Gather;
import org.zerock.triplet.domain.gather.entity.GatherInvite;
import org.zerock.triplet.domain.gather.entity.GatherMapping;
import org.zerock.triplet.domain.gather.repository.GatherInviteRepository;
import org.zerock.triplet.domain.gather.repository.GatherMappingRepository;
import org.zerock.triplet.domain.gather.repository.GatherRepository;
import org.zerock.triplet.domain.member.dto.MemberIdsByGatherDTO;
import org.zerock.triplet.domain.card.repository.MemberCardRepository;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.domain.member.dto.IdName;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatherService {

    private final GatherRepository gatherRepo;
    private final GatherMappingRepository gmRepo;
    private final MemberRepository memberRepo;
    private final MemberCardRepository cardRepo;
    private final GatherInviteRepository inviteRepo;
    @PersistenceContext
    private EntityManager em;

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

    // 모임장 : 이메일로 멤버 초대
    @Transactional
    public void createInviteByEmail(Long ownerId, InviteDTO.CreateByEmailRequest req) {
        final Long gatherId = req.getGatherId();
        final String email = req.getEmail().trim().toLowerCase();

        // 1) 방장 권한(서비스 내 JPQL)
        if (!isOwner(ownerId, gatherId)) {
            throw new AccessDeniedException("모임장만 초대할 수 있습니다.");
        }

        // 2) 이메일 → 회원ID (서비스 내 JPQL)
        Long invitedId = memberIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원이 아닙니다."));

        // 3) 이미 멤버인지 (레포 기존 existsByGatherAndMember 활용)
        if (isMemberOf(gatherId, invitedId)) {
            throw new IllegalStateException("이미 모임 멤버입니다.");
        }

        // 4) 같은 모임 대기/수락 중복 초대 방지 (기존 inviteRepo 메서드 사용)
        if (inviteRepo.existsByGatherIdAndInvitedIdAndStatus(gatherId, invitedId, 1)) {
            throw new IllegalStateException("이미 대기 중인 초대가 있습니다.");
        }
        if (inviteRepo.existsByGatherIdAndInvitedIdAndStatus(gatherId, invitedId, 2)) {
            throw new IllegalStateException("이미 수락된 초대가 있습니다.");
        }

        // 5) 저장
        GatherInvite gi = new GatherInvite();
        gi.setGatherId(gatherId);
        gi.setInvitedId(invitedId);
        gi.setStatus(1); // 대기
        inviteRepo.save(gi);
    }

    // 내 대기 초대 목록
    @Transactional(readOnly = true)
    public List<InviteDTO.Simple> getMyPendingInvites(Member me) {
        List<GatherInvite> list = inviteRepo.findMyPending(me.getId());

        Set<Long> gids = list.stream().map(GatherInvite::getGatherId).collect(Collectors.toSet());
        Map<Long, Gather> gmap = gatherRepo.findAllById(gids).stream()
                .collect(Collectors.toMap(Gather::getId, g -> g));

        return list.stream().map(gi -> InviteDTO.Simple.builder()
                .inviteId(gi.getInviteId())
                .gatherId(gi.getGatherId())
                .gatherName(Optional.ofNullable(gmap.get(gi.getGatherId()))
                        .map(Gather::getName).orElse("모임"))
                .status(gi.getStatus())
                .build()
        ).collect(Collectors.toList());
    }

    // 초대 수락
    @Transactional
    public void acceptInvite(Member me, Long inviteId) {
        GatherInvite gi = inviteRepo.findById(inviteId).orElseThrow();
        if (!Objects.equals(gi.getInvitedId(), me.getId())) {
            throw new AccessDeniedException("본인 초대가 아닙니다.");
        }
        gi.setStatus(2); // 수락

        // 레포 수정 없이 멤버 추가
        insertMemberIfNotExists(gi.getGatherId(), me.getId(), false);
    }

    // 초대 거절
    @Transactional
    public void rejectInvite(Member me, Long inviteId) {
        GatherInvite gi = inviteRepo.findById(inviteId).orElseThrow();
        if (!Objects.equals(gi.getInvitedId(), me.getId())) {
            throw new AccessDeniedException("본인 초대가 아닙니다.");
        }
        if (gi.getStatus() != 1) return;
        inviteRepo.delete(gi);
    }

    // 헬퍼
    // 방장 여부
    private boolean isOwner(Long memberId, Long gatherId) {
        Long cnt = em.createQuery(
                        "select count(gm) from GatherMapping gm " +
                                "where gm.gather.id = :gid and gm.member.id = :mid and gm.checkOwner = true",
                        Long.class
                ).setParameter("gid", gatherId)
                .setParameter("mid", memberId)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    // 모임 멤버 여부
    private boolean isMemberOf(Long gatherId, Long memberId) {
        Gather g = new Gather(); g.setId(gatherId);
        Member m = new Member(); m.setId(memberId);
        return gmRepo.existsByGatherAndMember(g, m);
    }

    // 이메일로 회원 ID 찾기
    private Optional<Long> memberIdByEmail(String emailLower) {
        List<Long> ids = em.createQuery(
                "select m.id from Member m where lower(m.email) = :em",
                Long.class
        ).setParameter("em", emailLower).setMaxResults(1).getResultList();
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    // gathers_mapping에 (없을 때만) 멤버 추가
    private void insertMemberIfNotExists(Long gatherId, Long memberId, boolean owner) {
        if (isMemberOf(gatherId, memberId)) return;

        Gather g = new Gather(); g.setId(gatherId);
        Member m = new Member(); m.setId(memberId);

        GatherMapping gm = new GatherMapping();
        gm.setGather(g);
        gm.setMember(m);
        gm.setCheckOwner(owner);

        gmRepo.save(gm);
    }
}
