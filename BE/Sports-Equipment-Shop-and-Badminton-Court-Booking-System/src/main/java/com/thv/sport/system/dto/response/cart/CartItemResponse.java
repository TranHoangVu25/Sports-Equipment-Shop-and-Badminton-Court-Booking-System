package com.thv.sport.system.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String image;
    private String sku;
    private String size;
}