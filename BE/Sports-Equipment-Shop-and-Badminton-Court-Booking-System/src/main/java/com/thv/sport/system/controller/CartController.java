package com.thv.sport.system.controller;

import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.request.cart.CartItemRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.cart.CartDetailResponse;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.model.CartItem;
import com.thv.sport.system.respository.CartRepository;
import com.thv.sport.system.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/cart")
@Slf4j
public class CartController {

    CartService cartService;
    CartRepository repository;

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
    ResponseEntity<ApiResponse<CartDetailResponse>> getCart(@AuthenticationPrincipal UserPrincipal user) {
        Integer userId = user.getUserId();
        return cartService.findByCartUserId(userId);
    }

    // tất cả cart
    @GetMapping("/get-carts")
    public List<Cart> getAllCart() {
        return cartService.findAllCart();
    }

    // thêm item vào cart
    @PostMapping
    public Cart createCartItem(
            @RequestBody @Valid CartItem cartItem,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Integer userId = user.getUserId();

        log.info("Add product: {}", cartItem.getName());

        return cartService.createCartItem(userId, cartItem);
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
    public ResponseEntity<ApiResponse<CartItem>> updateCartItem(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody @Valid CartItemRequest request
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