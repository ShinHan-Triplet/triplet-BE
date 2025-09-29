package org.zerock.triplet.domain.gather.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.gather.entity.GatherInvite;

import java.util.List;

public interface GatherInviteRepository extends JpaRepository<GatherInvite, Long> {

    // 내 대기 초대(알림 드롭다운)
    @Query("""
        SELECT gi FROM GatherInvite gi
        WHERE gi.invitedId = :memberId AND gi.status = 1
        ORDER BY gi.inviteId DESC
    """)
    List<GatherInvite> findMyPending(@Param("memberId") Long memberId);

    // 이미 멤버인지 여부
    boolean existsByGatherIdAndInvitedIdAndStatus(Long gatherId, Long invitedId, Integer status);


    // 같은 모임 대기 초대 존재 여부
    default boolean hasPending(Long gatherId, Long invitedId) {
        return existsByGatherIdAndInvitedIdAndStatus(gatherId, invitedId, 1);
    }

    // 같은 모임 과거 수락 이력 존재 여부
    default boolean hasAccepted(Long gatherId, Long invitedId) {
        return existsByGatherIdAndInvitedIdAndStatus(gatherId, invitedId, 2);
    }
}
