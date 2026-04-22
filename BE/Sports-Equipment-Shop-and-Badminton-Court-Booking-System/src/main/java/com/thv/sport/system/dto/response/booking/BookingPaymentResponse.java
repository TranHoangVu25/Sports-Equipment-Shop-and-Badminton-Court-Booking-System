package com.thv.sport.system.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingPaymentResponse {

    private Long bookingPaymentId;

    private BigDecimal amount;

    private String currency;

    private String paymentMethod;

    private String providerPaymentId;

    private String status;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
