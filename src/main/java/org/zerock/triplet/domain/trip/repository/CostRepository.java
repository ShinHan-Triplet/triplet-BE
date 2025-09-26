package org.zerock.triplet.domain.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.mytrip.dto.CostPlan;
import org.zerock.triplet.domain.trip.entity.Cost;

import java.util.List;

public interface CostRepository extends JpaRepository<Cost, Long> {
    @Query("""
        select new org.zerock.triplet.domain.mytrip.dto.CostPlan(
            c.day,
            c.food,
              c.transport,
              c.leisure,
              c.etc,
              c.checkPlan
        )
        from Cost c
        where c.trip.id = :tripId
        order by c.day asc
        """)
    List<CostPlan> findPlansByTripId(@Param("tripId") Long tripId);
}
