package com.thv.sport.system.dto.request.cart;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {
    String id;
    @Min(value = 1,message = "INVALID_QUANTITY")
    int quantity;
    boolean plus; // true: +1 , false: -1
    String sku;
    String size;
}
