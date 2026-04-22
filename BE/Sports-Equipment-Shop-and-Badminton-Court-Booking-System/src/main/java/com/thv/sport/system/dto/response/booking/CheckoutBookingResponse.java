package com.thv.sport.system.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutBookingResponse {
    private Long bookingId;
    private Long bookingPaymentId;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal paymentAmount;
}
