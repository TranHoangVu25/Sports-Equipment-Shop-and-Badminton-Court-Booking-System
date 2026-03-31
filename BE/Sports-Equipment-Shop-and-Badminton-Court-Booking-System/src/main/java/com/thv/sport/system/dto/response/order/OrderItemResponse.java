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
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String sku;
    private String size;
    private String imgUrl;
}
