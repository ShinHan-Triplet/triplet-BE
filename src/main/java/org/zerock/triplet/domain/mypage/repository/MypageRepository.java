package org.zerock.triplet.domain.mypage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.gather.entity.Gather;

import java.util.Collection;
import java.util.List;

public interface MypageRepository extends JpaRepository<Gather, Long> {
    /** 내가 속한 모임 목록 (대표카드까지 한 번에 로딩) */
    @Query("""
        select distinct g
        from GatherMapping gm
        join gm.gather g
        left join fetch g.mcard mc
        where gm.member.id = :memberId
        order by g.id
    """)
    List<Gather> findMyGathers(@Param("memberId") Long memberId);

    /** 주어진 모임들에 속한 멤버 이름을 (모임ID, 이름) 로 반환 */
    @Query("""
        select gm.gather.id, m.name
        from GatherMapping gm
        join gm.member m
        where gm.gather.id in :gatherIds
        order by gm.gather.id, m.name
    """)
    List<Object[]> findMemberNamesByGatherIds(@Param("gatherIds") Collection<Long> gatherIds);

    /** 주어진 모임들 중, 내가 OWNER 인 모임 ID들 */
    @Query("""
        select distinct gm.gather.id
        from GatherMapping gm
        where gm.member.id = :memberId
          and gm.gather.id in :gatherIds
          and gm.checkOwner = true
    """)
    List<Long> findOwnerGatherIds(@Param("memberId") Long memberId,
                                  @Param("gatherIds") Collection<Long> gatherIds);
}
