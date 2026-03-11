package com.thv.sport.system.dto.request.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemRequest {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String image;
    private String variantId;
}
