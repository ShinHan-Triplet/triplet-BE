// src/main/java/org/zerock/triplet/domain/mycard/repository/UsageRepository.java
package org.zerock.triplet.domain.mycard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.card.entity.CardUsage;

import java.util.Optional;

public interface UsageRepository extends JpaRepository<CardUsage, Long> {

    @Query("""
      select u
      from MemberCard mc
      join mc.usages u
      where mc.member.id = :memberId
        and mc.id        = :mcardId
        and u.id         = :usageId
    """)
    Optional<CardUsage> findOwnedInCard(@Param("memberId") Long memberId,
                                        @Param("mcardId") Long mcardId,
                                        @Param("usageId") Long usageId);

//    @Query("""
//        select u
//        from MemberCard mc
//        join mc.usages u
//        where mc.member.id = :memberId
//          and u.id = :usageId
//    """)
//    Optional<CardUsage> findOwned(@Param("memberId") Long memberId,
//                              @Param("usageId") Long usageId);
}
