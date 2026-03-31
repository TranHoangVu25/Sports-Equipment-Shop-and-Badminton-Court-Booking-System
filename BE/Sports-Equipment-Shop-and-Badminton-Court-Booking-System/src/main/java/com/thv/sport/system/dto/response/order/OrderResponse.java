package com.thv.sport.system.dto.response.order;

import com.thv.sport.system.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private LocalDateTime createdAt;
    private String locationDetail;
    private BigDecimal totalAmount;
    private String status;
    private String recipient;
    private String phoneNumber;
    private BigDecimal discount;
    private BigDecimal subtotal;
    private List<OrderItemResponse> orderItems;
}
