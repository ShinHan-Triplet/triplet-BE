package org.zerock.triplet.domain.gather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.zerock.triplet.domain.card.entity.MemberCard;

@Entity
@Table(name = "gathers")
@Data
public class Gather {
    @Id @Column(name="gather_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="gather_name")
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="mcard_id")
    private MemberCard mcard;


}
