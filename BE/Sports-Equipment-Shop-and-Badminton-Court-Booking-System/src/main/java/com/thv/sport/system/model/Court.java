package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "court")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_center_id", nullable = false)
    private CourtCenter courtCenter;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price_per_hour", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "status", nullable = false)
    private Integer status;
}

