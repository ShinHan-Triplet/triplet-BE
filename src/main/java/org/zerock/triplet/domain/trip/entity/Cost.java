package org.zerock.triplet.domain.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cost")
@Data
@NoArgsConstructor
public class Cost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cost_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "trip_day", nullable = false)
    private Integer day; //1일부터 N일

    @Column(name = "food_cost", nullable = false)
    private Long food;

    @Column(name = "transport_cost", nullable = false)
    private Long transport;

    @Column(name = "leisure_cost", nullable = false)
    private Long leisure;

    @Column(name = "etc_cost", nullable = false)
    private Long etc;

    @Column(name = "check_plan", nullable = false)
    private Boolean checkPlan;

}
