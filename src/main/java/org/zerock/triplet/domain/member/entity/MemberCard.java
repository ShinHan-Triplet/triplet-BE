package org.zerock.triplet.domain.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.zerock.triplet.domain.card.entity.Card;


@Entity
@Table(name="member_card")
@Data
public class MemberCard {
    @Id @Column(name="mcard_id")
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="card_id")
    private Card card;

    @Column(name="card_nickname")
    private String cardNickname;

    @Column(name="card_num")
    private String cardNum;

    @Column(name="check_gather")
    private Boolean checkGather; // 모임 대표카드 여부
}
