package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "price_per_hour", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
}

