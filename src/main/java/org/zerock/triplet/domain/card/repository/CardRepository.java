package org.zerock.triplet.domain.card.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.triplet.domain.card.entity.Card;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    @EntityGraph(attributePaths = "benefits")
    List<Card> findByTheme(Integer theme);
}
