package org.zerock.triplet.domain.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
public class Trip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(name = "trip_name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "gather_id", nullable = false)
    private Long gatherId;               // 간단히 FK 값만 저장 (필요시 @ManyToOne로 변경)

    @Column(name = "themes", nullable = false)
    private Integer theme;               // 1~4

    @Column(name = "total_cost", nullable = false)
    private Long totalCost;              // 원화 정수로 관리

    @Column(name = "stay_cost", nullable = false)
    private Long stayCost;

    @Column(name = "insurance_cost", nullable = false)
    private Long insuranceCost;

    @Column(name = "trip_img", nullable = true, length = 120)
    private String tripImg;  // s3 최종 키 또는 파일명
}
