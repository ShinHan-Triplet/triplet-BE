package org.zerock.triplet.domain.card.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "benefits")
public class Benefit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="benefit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="card_id")
    private Card card;

    @Column(name="benefit_title")
    private String benefitTitle;

    @Column(name="benefit_content")
    private String benefitContent;

    @Column(name="short_title")
    private String shortTitle;

    @Column(name="short_content")
    private String shortContent;
}
