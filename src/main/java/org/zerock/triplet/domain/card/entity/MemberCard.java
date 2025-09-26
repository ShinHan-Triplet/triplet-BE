package org.zerock.triplet.domain.card.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.zerock.triplet.domain.gather.entity.Gather;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.List;


@Entity
@Table(name="member_card")
@Data
public class MemberCard {
    @Id @Column(name="mcard_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="card_id")
    private Card card;

    @Column(name="card_num")
    private String cardNum;

    @Column(name="card_pw")
    private String pw;

    @Column(name="card_nickname")
    private String cardNickname;

    @Column(name = "card_status")
    private Integer cardStatus;

    @Column(name = "account")
    private String account;

    @Column(name="check_gather")
    private Boolean checkGather; // 모임 대표카드 여부

    @Column(name="member_num")
    private String memberNum;

    @Column(name="member_address")
    private String address;

    @Column(name="member_phone")
    private String phone;

    @OneToMany(mappedBy = "memberCard", fetch = FetchType.LAZY)
    private List<CardUsage> usages;
}
