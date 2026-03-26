package com.thv.sport.system.dto.response.order;

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
public class CheckoutResponse {
    private Long orderId;
    private Long paymentId;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal paymentAmount;
}
