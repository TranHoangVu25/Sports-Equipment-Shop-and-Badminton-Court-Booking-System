package com.thv.sport.system.service.impl;

import com.thv.sport.system.dto.request.cart.CartItemRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.exception.ErrorCode;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.model.CartItem;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.CartItemRepository;
import com.thv.sport.system.respository.CartRepository;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    ProductRepository productRepository;
    CartItemRepository cartItemRepository;

    @Override
    public void createCart(Integer userId) {
        List<CartItem> cartItemList = new ArrayList<>();
        User user = new User();
        user.setUserId(Long.valueOf(userId));

        Cart cart = Cart
                .builder()
                .user(user)
                .cartItems(cartItemList)
                .build();
        cartRepository.save(cart);
    }

    @Override
    public Cart findCartById(String id) {
        return cartRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("cart id not found"));
    }

    @Override
    public List<Cart> findAllCart() {
        return cartRepository.findAll();
    }

    //thêm cart item vào giỏ hàng
    @Override
    public Cart createCartItem(Integer userId, CartItem newItem) {
        boolean a = cartRepository.existsByUserUserId(userId);
        log.info("result =" + a);
        //check giỏ hàng đã tồn tại chưa, nếu chưa thì tạo mới
        if (!cartRepository.existsByUserUserId(userId)) {

            User user = new User();
            user.setUserId(Long.valueOf(userId));

            Cart cart = Cart.builder()
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .build();

            cartRepository.save(cart);
        }

        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User have id: " + userId + " not found cart."));

        List<CartItem> existingCartItems = cart.getCartItems();

        boolean found = false;

        //kiểm tra id prod, chưa -> thêm mới, tồn tại -> += quantity
        for (CartItem item : existingCartItems) {
            if (newItem.getCartItemId().equals(item.getCartItemId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                item.setPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                found = true;
                break;
            }
        }
        if (!found) existingCartItems.add(newItem);
        return cartRepository.save(cart);
    }

    @Override
    public ResponseEntity<ApiResponse<String>> deleteCartItem(Integer userId, String cartItemId) {

        // 1. Lấy cart của user
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 2. Tìm cartItem theo cartItemId
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getCartItemId().equals(Long.valueOf(cartItemId)))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .message("Cart item not found")
                            .build());
        }

        // 3. Xóa item khỏi cart
        cart.getCartItems().remove(cartItem);

        // 4. Save cart
        cartRepository.save(cart);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Delete cart item successfully")
                        .build()
        );
    }

    @Override
    public ResponseEntity<ApiResponse<CartItem>> updateQuantity(Integer userId, CartItemRequest request) {

        // 1. Kiểm tra cart của user
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 2. Tìm cartItem theo id
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getCartItemId().equals(Long.valueOf(request.getId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int currentQuantity = cartItem.getQuantity();

        // 3. Nếu plus = true -> tăng
        if (request.isPlus()) {
            cartItem.setQuantity(currentQuantity + 1);
            CartItem savedItem = cartItemRepository.save(cartItem);

            return ResponseEntity.ok(
                    ApiResponse.<CartItem>builder()
                            .message("Increase quantity successfully")
                            .result(savedItem)
                            .build()
            );
        }

        // 4. Nếu plus = false -> giảm
        int newQuantity = currentQuantity - 1;

        // Nếu quantity = 0 -> xóa khỏi cart
        if (newQuantity <= 0) {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);

            return ResponseEntity.ok(
                    ApiResponse.<CartItem>builder()
                            .message("Item removed from cart")
                            .build()
            );
        }

        cartItem.setQuantity(newQuantity);
        CartItem savedItem = cartItemRepository.save(cartItem);

        return ResponseEntity.ok(
                ApiResponse.<CartItem>builder()
                        .message("Decrease quantity successfully")
                        .result(savedItem)
                        .build()
        );
    }

    @Override
    public Cart findByUserId(Integer userId) {
        return cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User id not found"));
    }

    @Override
    public Cart findByUserIdToCreate(Integer userId) {
        return cartRepository.findByUserId(Long.valueOf(userId))
                .orElse(null);
    }


    @Override
    public ApiResponse<String> checkout(Integer userId, String jwtToken) {
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("id not found"));

        List<CartItem> cartItems = cart.getCartItems();


        return ApiResponse.<String>builder()
                .message("Đã gửi dữ liệu sang order_service")
                .result(null)
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<Cart>> addToCart(Integer userId, CartItemRequest request) {

        // 1. Lấy product và kiểm tra trạng thái
        Product product = productRepository.findById(Long.valueOf(request.getId()))
                .filter(p -> "còn hàng".equalsIgnoreCase(p.getStatus()))
                .orElseThrow(() -> new RuntimeException("Product not available"));

        // 2. Lấy cart của user hoặc tạo mới
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseGet(() -> {
                    User user = User.builder()
                            .userId(Long.valueOf(userId))
                            .build();

                    Cart newCart = Cart.builder()
                            .user(user)
                            .cartItems(new ArrayList<>())
                            .build();

                    return cartRepository.save(newCart);
                });

        // 3. Kiểm tra product đã có trong cart chưa
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {

            // 4. Nếu đã có -> tăng quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());

        } else {

            // 5. Nếu chưa có -> tạo mới CartItem
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .image(
                            product.getProductImages() != null &&
                                    !product.getProductImages().isEmpty()
                                    ? product.getProductImages().getFirst().getImageUrl()
                                    : null
                    )
                    .description(product.getDescription())
                    .build();

            cart.getCartItems().add(newItem);
        }

        // 6. Save cart (cascade sẽ save cartItem)
        Cart savedCart = cartRepository.save(cart);

        return ResponseEntity.ok(
                ApiResponse.<Cart>builder()
                        .result(savedCart)
                        .message(ErrorCode.ADD_TO_CART_SUCCESS.getMessage())
                        .build()
        );
    }
}