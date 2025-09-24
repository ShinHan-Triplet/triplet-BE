package org.zerock.triplet.domain.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.triplet.domain.trip.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
