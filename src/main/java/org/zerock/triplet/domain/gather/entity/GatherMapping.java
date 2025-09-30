package org.zerock.triplet.domain.gather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.zerock.triplet.domain.member.entity.Member;

@Entity
@Table(name = "gathers_mapping")
@Data
public class GatherMapping {
    @Id @Column(name="gm_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="gather_id")
    private Gather gather;

    @Column(name="check_owner")
    private Boolean checkOwner;
}
