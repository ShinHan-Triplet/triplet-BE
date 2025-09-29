package org.zerock.triplet.domain.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.gather.dto.MemberCardDTO;
import org.zerock.triplet.domain.card.entity.MemberCard;

import java.util.List;

public interface MemberCardRepository extends JpaRepository<MemberCard, Long> {

    // 개인카드만 (모임카드가 아닌 카드)
    @Query("""
        select new org.zerock.triplet.domain.gather.dto.MemberCardDTO(
            mc.id,
            mc.cardNickname,
            mc.cardNum,
            c.id
        )
        from MemberCard mc
        join mc.card c
        where mc.member.id = :memberId
            and(mc.checkGather = false or mc.checkGather is null)
            and mc.cardStatus = 1
    """)
    List<MemberCardDTO> findPersonalCards(@Param("memberId") Long memberId);

    MemberCard findMemberCardById(Long cardId);

    boolean existsByCardNum(String cardNum);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update MemberCard mc
           set mc.checkGather = :check
         where mc.id = :cardId
    """)
    void markCheckGather(@Param("cardId") Long cardId, @Param("check") Boolean check);
}
