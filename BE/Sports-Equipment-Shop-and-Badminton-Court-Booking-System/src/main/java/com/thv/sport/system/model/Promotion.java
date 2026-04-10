package com.thv.sport.system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "value", precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "code", length = 255, nullable = false)
    private String code;

    @Column(name = "is_used", nullable = false)
    private Integer isUsed;

    @Column(name = "stripe_coupon_id", length = 255)
    private String stripeCouponId;

    @Column(name = "stripe_promotion_id", length = 255)
    private String stripePromotionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;
}

