package com.thv.sport.system.dto.response.cart;

import com.thv.sport.system.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDetailResponse {
    private Long cartId;
    List<CartItem> cartItems;
    BigDecimal totalPrice;
}
