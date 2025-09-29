package org.zerock.triplet.domain.gather.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "gather_invite")
@Getter
@Setter
public class GatherInvite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long inviteId;

    @Column(name = "invited_id", nullable = false)
    private Long invitedId;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "gather_id", nullable = false)
    private Long gatherId;
}