package org.zerock.triplet.domain.card.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="card_usage")
@Data
public class CardUsage {

    @Id @Column(name="usage_id")
    private Long id;

    @Column(name="category")
    private Integer category;

    @Column(name="usage_cost")
    private Integer cost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name="cost_date")
    private LocalDateTime date;

    @Column(name="memo")
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcard_id")
    private MemberCard memberCard;
}
