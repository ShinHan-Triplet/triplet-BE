package org.zerock.triplet.domain.mycard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.card.entity.MemberCard;
import org.zerock.triplet.domain.mycard.dto.BenefitDTO;
import org.zerock.triplet.domain.mycard.dto.MycardHistoryResponse;
import org.zerock.triplet.domain.mycard.dto.MycardListItemDTO;
import org.zerock.triplet.domain.mycard.dto.UsageItemDTO;

import java.util.List;
import java.util.Optional;

public interface MycardRepository extends JpaRepository<MemberCard, Long> {
    public MemberCard findMemberCardById(Long id);

    /** 내 카드 목록 (member 기준) */
    @Query("""
        select new org.zerock.triplet.domain.mycard.dto.MycardListItemDTO(
            mc.id,
            c.id,
            c.name,
            mc.cardNickname,
            mc.cardStatus,
            mc.cardNum,
            mc.account,
            mc.checkGather
        )
        from MemberCard mc
        join mc.card c
        where mc.member.id = :memberId
        order by mc.id desc
    """)
    List<MycardListItemDTO> findMyCardsByMemberId(@Param("memberId") Long memberId);

    /** 단일 카드 헤더 (소유 검증: member + mcard) */
    @Query("""
        select new org.zerock.triplet.domain.mycard.dto.MycardHistoryResponse(
            mc.id,
            c.id,
            c.name,
            mc.cardNickname,
            mc.cardNum,
            mc.account
        )
        from MemberCard mc
        join mc.card c
        where mc.member.id = :memberId
          and mc.id        = :mcardId
    """)
    Optional<MycardHistoryResponse> findMyCardHeader(@Param("memberId") Long memberId,
                                                                        @Param("mcardId") Long mcardId);

    /** 혜택: mcardId → card → benefits (소유 검증 포함) */
    @Query("""
        select new org.zerock.triplet.domain.mycard.dto.BenefitDTO(
            b.benefitTitle, b.benefitContent
        )
        from MemberCard mc
        join mc.card c
        join c.benefits b
        where mc.id        = :mcardId
        order by b.id asc
    """)
    List<BenefitDTO> findBenefitsByMcard(@Param("mcardId") Long mcardId);

    /** 사용 내역: 소유 검증 포함 */
    @Query("""
        select new org.zerock.triplet.domain.mycard.dto.UsageItemDTO(
            u.id,
            u.category,
            u.cost,
            u.date,
            u.memo,
            mc.id
        )
        from MemberCard mc
        join mc.usages u
        where mc.member.id = :memberId
          and mc.id        = :mcardId
        order by u.date desc, u.id desc
    """)
    List<UsageItemDTO> findUsagesByMcardId(@Param("memberId") Long memberId,
                                           @Param("mcardId") Long mcardId);
}
