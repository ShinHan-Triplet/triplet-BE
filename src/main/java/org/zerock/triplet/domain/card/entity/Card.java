package org.zerock.triplet.domain.card.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="cards")
@Data
public class Card {
    @Id @Column(name="card_id")
    private Long id;

    @Column(name="card_name")
    private String name;

    @Column(name="card_intro")
    private String intro;

    @Column(name="card_desc")
    private String desc;

    @Column(name="card_img")
    private String img;

    @Column(name="card_theme")
    private Integer theme;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Benefit> benefits = new ArrayList<>();
}
