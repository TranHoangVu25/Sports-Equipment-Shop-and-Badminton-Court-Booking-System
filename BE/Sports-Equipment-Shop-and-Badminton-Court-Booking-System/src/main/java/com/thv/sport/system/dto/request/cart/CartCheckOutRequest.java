package com.thv.sport.system.dto.request.cart;

import com.thv.sport.system.model.Address;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartCheckOutRequest {
    List<CheckoutItemRequest> items;
    Address address;
    //String promotion;
}
