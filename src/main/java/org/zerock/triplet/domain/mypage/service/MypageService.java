package org.zerock.triplet.domain.mypage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.gather.entity.Gather;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.domain.mypage.dto.MypageDTO;
import org.zerock.triplet.domain.mypage.repository.MypageRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final MemberRepository memberRepository;
    private final MypageRepository mypageRepository;

    // ===== 공통 유틸 =====
    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    public MypageDTO.MyInfoResponse getMyInfo(Long memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
        return MypageDTO.MyInfoResponse.builder()
                .name(m.getName())
                .birthday(m.getBirthday())
                .profileImage(m.getProfileImage())
                .build();
    }

    public List<MypageDTO.MyGatherSummary> getMyGathers(Long memberId) {
        // 1) 내가 속한 모임(+대표카드) 한 번에 로딩
        List<Gather> gathers = mypageRepository.findMyGathers(memberId);
        if (gathers.isEmpty()) return List.of();

        List<Long> gatherIds = gathers.stream().map(Gather::getId).toList();

        // 2) 각 모임의 멤버 이름들 로딩 (JPQL 스칼라)
        Map<Long, List<String>> namesByGather = mypageRepository
                .findMemberNamesByGatherIds(gatherIds).stream()
                .collect(Collectors.groupingBy(
                        r -> ((Number) r[0]).longValue(),
                        Collectors.mapping(r -> String.valueOf(r[1]), Collectors.toList())
                ));

        // 3) 내가 OWNER인 모임 식별
        Set<Long> ownerSet = new HashSet<>(mypageRepository.findOwnerGatherIds(memberId, gatherIds));

        // 4) DTO 조립
        return gathers.stream().map(g -> {
            var names = namesByGather.getOrDefault(g.getId(), List.of());
            var nickname = g.getMcard().getCardNickname();

            return MypageDTO.MyGatherSummary.builder()
                    .gatherId(g.getId())
                    .gatherName(g.getName())
                    .owner(ownerSet.contains(g.getId()))
                    .members(names)
                    .cardNickname(nickname)
                    .build();
        }).toList();
    }

    // 모임 삭제(모임장 전용)
    @Transactional
    public void deleteGatherForOwner(Long gatherId, Long memberId) {
        // 존재 확인 (Optional)
        if (!mypageRepository.existsById(gatherId)) {
            throw new IllegalArgumentException("존재하지 않는 모임입니다.");
        }

        // 권한 확인
        boolean isOwner = mypageRepository.isOwner(gatherId, memberId);
        if (!isOwner) {
            throw new AccessDeniedException("모임 삭제 권한이 없습니다. (모임장만 가능)");
        }

        // 삭제
        mypageRepository.deleteById(gatherId);
    }

    // 모임 탈퇴(구성원 전용)
    @Transactional
    public void leaveGather(Long gatherId, Long memberId) {
        // 존재 확인 (Optional)
        if (!mypageRepository.existsById(gatherId)) {
            throw new IllegalArgumentException("존재하지 않는 모임입니다.");
        }

        // 구성원 여부 확인
        boolean isMember = mypageRepository.isMember(gatherId, memberId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 모임 구성원이 아닙니다.");
        }

        // 오너는 탈퇴 불가 (정책)
        if (mypageRepository.isOwner(gatherId, memberId)) {
            // 선택지:
            // 1) 그냥 막는다
            // 2) 멤버가 본인 1명뿐이면 삭제로 대체한다 (원하면 이 로직으로 바꿔줄게)
            throw new AccessDeniedException("모임장은 탈퇴할 수 없습니다. 모임 삭제를 사용해 주세요.");
        }

        // 매핑 삭제 (본인 탈퇴)
        int affected = mypageRepository.deleteMapping(gatherId, memberId);
        if (affected == 0) {
            // 이 경우는 이론상 거의 없음(동시성 등)
            throw new IllegalStateException("탈퇴 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
