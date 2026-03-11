package com.thv.sport.system.service;


import com.thv.sport.system.dto.request.cart.CartItemRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.model.CartItem;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    void createCart(Integer userId);

    Cart findCartById(String id);

    List<Cart> findAllCart();

    //thêm cart item vào giỏ hàng
    Cart createCartItem(Integer userId, CartItem newItem);

    ResponseEntity<ApiResponse<String>> deleteCartItem(Integer userId, String productionId);

    ResponseEntity<ApiResponse<CartItem>> updateQuantity(Integer userId, CartItemRequest request);

    Cart findByUserId(Integer userId);

    Cart findByUserIdToCreate(Integer userId);

    ApiResponse<String> checkout(Integer userId,String jwtToken);

    ResponseEntity<ApiResponse<Cart>> addToCart(Integer userId, CartItemRequest request);
}
