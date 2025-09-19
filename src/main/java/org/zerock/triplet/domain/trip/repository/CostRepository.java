package org.zerock.triplet.domain.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.triplet.domain.trip.entity.Cost;

public interface CostRepository extends JpaRepository<Cost, Long> {
}
