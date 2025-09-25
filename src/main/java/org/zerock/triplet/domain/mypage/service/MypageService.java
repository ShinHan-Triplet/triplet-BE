package org.zerock.triplet.domain.mypage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}
