package org.zerock.triplet.domain.gather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.triplet.domain.gather.entity.Gather;

public interface GatherRepository extends JpaRepository<Gather, Long> {
    Gather findGatherById(Long gatherId);
}
