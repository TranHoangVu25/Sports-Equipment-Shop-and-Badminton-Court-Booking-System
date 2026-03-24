package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.request.cart.CartItemRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.cart.CartItemResponse;
import com.thv.sport.system.dto.response.cart.CartResponse;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping(Constants.ApiPath.API_CART)
@Slf4j
public class CartController {

    CartService cartService;

    // tạo cart nếu chưa có
    @GetMapping
    public ResponseEntity<?> createCart(@AuthenticationPrincipal UserPrincipal user) {

        if (user == null) {
            log.warn("User is null");
            return ResponseEntity.badRequest().body("User not authenticated");
        }

        Integer userId = user.getUserId();
        log.info("User ID: {}", userId);

        Cart cart = cartService.findByUserIdToCreate(userId);

        if (cart == null) {
            cartService.createCart(userId);
            return ResponseEntity.ok("Cart created for user " + userId);
        }

        return ResponseEntity.ok("Cart already exists for user " + userId);
    }

    // lấy cart theo user
    @GetMapping("/get-user-cart")
    public
    ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal user) {
        Integer userId = user.getUserId();
        return cartService.findByCartUserId(userId);
    }

    // tất cả cart
    @GetMapping("/get-carts")
    public List<Cart> getAllCart() {
        return cartService.findAllCart();
    }

    // xóa cart item
    @DeleteMapping("/cart-items/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteCartItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String productId
    ) {

        Integer userId = user.getUserId();
        return cartService.deleteCartItem(userId, productId);
    }

    // update quantity
    @PutMapping("/cart-items")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CartItemRequest request
    ) {
        Integer userId = user.getUserId();
        return cartService.updateQuantity(userId, request);
    }

    // add to cart
    @PostMapping("/add-to-cart")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @RequestBody @Valid CartItemRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Integer userId = user.getUserId();

        log.info("UserId: {}", userId);

        return cartService.addToCart(userId, request);
    }
}