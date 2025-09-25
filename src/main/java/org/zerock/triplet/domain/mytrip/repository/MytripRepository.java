package org.zerock.triplet.domain.mytrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.mytrip.dto.*;
import org.zerock.triplet.domain.trip.entity.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MytripRepository extends JpaRepository<Trip, Long> {
    /** 내가 속한 모임들의 Trip 엔티티 */
    @Query("""
        select t
        from Trip t
        where exists (
            select 1 from GatherMapping gm
            where gm.gather.id = t.gatherId
              and gm.member.id = :memberId
        )
        order by t.startDate desc, t.id desc
    """)
    List<Trip> findTripsICanAccess(@Param("memberId") Long memberId);

    /** 여러 모임의 (모임ID, 모임명) */
    @Query("""
        select g.id, g.name
        from Gather g
        where g.id in :gatherIds
    """)
    List<Object[]> findGatherNames(@Param("gatherIds") Collection<Long> gatherIds);

    /** 여러 모임의 (모임ID, 멤버ID, 멤버명) */
    @Query("""
        select gm.gather.id, m.id, m.name
        from GatherMapping gm
        join gm.member m
        where gm.gather.id in :gatherIds
        order by gm.gather.id, m.name
    """)
    List<Object[]> findMembersByGatherIds(@Param("gatherIds") Collection<Long> gatherIds);

    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.TripMember(
            m.id, m.name
        )
        from GatherMapping gm
        join gm.member m
        where gm.gather.id = :gatherId
        order by m.name asc
    """)
    List<TripMember> findMembersByGatherId(@Param("gatherId") Long gatherId);

    /** 내가 OWNER(또는 checkOwner=true) 인 모임 ID 집합 */
    @Query("""
        select distinct gm.gather.id
        from GatherMapping gm
        where gm.gather.id in :gatherIds
          and gm.member.id = :memberId
          and gm.checkOwner = true
    """)
    List<Long> findOwnerGatherIds(@Param("memberId") Long memberId,
                                  @Param("gatherIds") Collection<Long> gatherIds);

    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.TripHeader(
            t.id, t.name, t.startDate, t.endDate,
            t.theme, t.totalCost, t.tripImg,
            t.gatherId, g.name
        )
        from Trip t
        join Gather g on g.id = t.gatherId
        where t.id = :tripId
          and exists (
              select 1
              from GatherMapping gm
              where gm.gather.id = t.gatherId
                and gm.member.id = :memberId
          )
    """)
    Optional<TripHeader> findTripHeader(@Param("tripId") Long tripId,
                                        @Param("memberId") Long memberId);

    /** (상세) 멤버 목록: gatherId 기준 */
    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.TripMember(
            m.id, m.name
        )
        from GatherMapping gm
        join gm.member m
        where gm.gather.id = :gatherId
        order by m.name asc
    """)
    List<TripMember> findTripMembers(@Param("gatherId") Long gatherId);

    /** (상세) 연결 카드: gather → mcard → card */
    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.TripCard(
            mc.id, c.id, c.name, mc.cardNickname, mc.cardNum, mc.account, mc.cardStatus
        )
        from Gather g
        join g.mcard mc
        join mc.card c
        where g.id = :gatherId
    """)
    Optional<TripCard> findTripCard(@Param("gatherId") Long gatherId);

    /** (상세) 내가 OWNER인지 여부 (count 를 long 으로!) */
    @Query("""
        select count(gm)
        from GatherMapping gm
        where gm.gather.id = :gatherId
          and gm.member.id = :memberId
    """)
    long countOwner(@Param("gatherId") Long gatherId,
                    @Param("memberId") Long memberId);

    @Query("""
    select count(gm)
    from GatherMapping gm
    where gm.gather.id = (select t.gatherId from Trip t where t.id = :tripId)
      and gm.member.id = :memberId
""")
    long existsTripMember(@Param("tripId") Long tripId,
                          @Param("memberId") Long memberId);

    @Query("""
    select g.mcard.id
    from Gather g
    where g.id = (select t.gatherId from Trip t where t.id = :tripId)
""")
    Optional<Long> findTripMcardId(@Param("tripId") Long tripId);

    @Query("""
    select new org.zerock.triplet.domain.mytrip.dto.UsageItem(
        u.id,
        u.category,
        case u.category
            when 1 then '숙박'
            when 2 then '보험'
            when 3 then '식비'
            when 4 then '교통비'
            when 5 then '여가비'
            else '기타'
        end,
        cast(u.cost as long),
        u.memo,
        u.date
    )
    from CardUsage u
    where u.memberCard.id = :mcardId
      and u.date >= :from
      and u.date <  :to
    order by u.date asc, u.id asc
""")
    List<UsageItem> findUsagesForDay(@Param("mcardId") Long mcardId,
                                    @Param("from") java.time.LocalDateTime from,
                                    @Param("to")   java.time.LocalDateTime to);

    @Query("""
    select coalesce(sum(cast(u.cost as long)), 0)
    from CardUsage u
    where u.memberCard.id = :mcardId
      and u.date >= :from
      and u.date <  :to
""")
    Long sumUsagesForDay(@Param("mcardId") Long mcardId,
                         @Param("from") java.time.LocalDateTime from,
                         @Param("to")   java.time.LocalDateTime to);

    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.TripCard(
            mc.id, c.id, c.name, mc.cardNickname, mc.cardNum, mc.account, mc.cardStatus
        )
        from Trip t
        join Gather g      on g.id = t.gatherId
        join g.mcard mc
        join mc.card c
        where t.id = :tripId
    """)
    Optional<TripCard> findGatherCardByTripId(@Param("tripId") Long tripId);

    @Query("""
        select t
        from Trip t
        where t.id = :tripId
    """)
    Optional<Trip> findTripPlanBasics(@Param("tripId") Long tripId);

    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.BudgetTotal(
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long),
            cast(0 as long)
        )
        from Trip t
        where t.id = :tripId
          and :plannedFlag = :plannedFlag
    """)
    Optional<org.zerock.triplet.domain.mytrip.dto.BudgetTotal> findBudgetTotal(
            @Param("tripId") Long tripId,
            @Param("plannedFlag") Integer plannedFlag
    );
}
