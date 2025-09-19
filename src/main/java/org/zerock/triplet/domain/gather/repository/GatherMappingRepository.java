package org.zerock.triplet.domain.gather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.zerock.triplet.domain.gather.entity.GatherMapping;
import org.zerock.triplet.domain.member.dto.MemberIdsByGatherDTO;

import java.util.List;

public interface GatherMappingRepository extends JpaRepository<GatherMapping, Long> {

    // 내가 방장인 모임 id들
    @Query("""
        select gm.gather.id
        from GatherMapping gm
        where gm.member.id = :memberId 
            and gm.checkOwner = true
            and exists(
                select 1 from GatherMapping gm2
                where gm2.gather.id = gm.gather.id
                    and gm2.member.id <> :memberId
            )
    """)
    List<Long> findOwnedGatherIds(@Param("memberId") Long memberId);

    // 나만 있는 모임 id들
    @Query("""
        select gm1.gather.id
        from GatherMapping gm1
        group by gm1.gather.id
        having count(gm1.id) = 1 and min(case when gm1.member.id = :memberId then 1 else 0 end) = 1
    """)
    List<Long> findSoloGatherIds(@Param("memberId") Long memberId);

    // 여러 모임에 속한 멤버 id를 한 번에 가져오기
    @Query("""
   select new org.zerock.triplet.domain.member.dto.MemberIdsByGatherDTO(
     gm.gather.id, gm.member.id
   )
   from GatherMapping gm
   where gm.gather.id in :gatherIds
""")
    List<MemberIdsByGatherDTO> findMemberIdsByGatherIds(@Param("gatherIds") List<Long> gatherIds);
}
