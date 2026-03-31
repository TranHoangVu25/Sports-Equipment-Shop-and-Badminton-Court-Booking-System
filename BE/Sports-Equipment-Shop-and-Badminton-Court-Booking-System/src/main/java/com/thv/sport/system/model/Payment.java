package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    //price before using promotion code
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

