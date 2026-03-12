package com.thv.sport.system.dto.response.order;

import com.thv.sport.system.model.Product;
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
public class OrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private Product productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
