package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.common.DateUtil;
import com.thv.sport.system.common.MessageUtils;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.dto.response.order.OrderItemResponse;
import com.thv.sport.system.dto.response.order.OrderResponse;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.model.CartItem;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.model.OrderItem;
import com.thv.sport.system.model.Payment;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.CartItemRepository;
import com.thv.sport.system.respository.CartRepository;
import com.thv.sport.system.respository.OrderRepository;
import com.thv.sport.system.respository.PaymentRepository;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    CartRepository cartRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    PaymentRepository paymentRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(Long userId, OrderRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        String checkoutType;
        if (request.getCheckoutType()) {
            checkoutType = Constants.CheckoutMethod.COD;
        } else {
            checkoutType = Constants.CheckoutMethod.STRIPE;
        }
        User u = new User();
        u.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        // 1. tạo order
        Order order = new Order();
        order.setUser(u);
        order.setStatus(Constants.OrderStatus.PENDING);
        order.setRecipient(request.getRecipient());
        order.setLocationDetail(request.getLocationDetail());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setEmail(request.getEmail());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2. cartItem -> orderItem
        for (CartItem cartItem : cart.getCartItems()) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setSku(cartItem.getSku());
            orderItem.setSize(cartItem.getSize());

            BigDecimal subTotal = cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            orderItem.setSubTotal(subTotal);

            totalAmount = totalAmount.add(subTotal);

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 3. save order
        Order savedOrder = orderRepository.save(order);

        // 4. tạo payment (COD)
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        payment.setAmount(totalAmount);
        payment.setCurrency(Constants.Currency.VND);
        payment.setPaymentMethod(checkoutType);
        payment.setStatus(Constants.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // 5. clear cart
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        return ResponseEntity.ok(
                ApiResponse.<CheckoutResponse>builder()
                        .message("Checkout successfully")
                        .result(CheckoutResponse.builder()
                                .orderId(order.getOrderId())
                                .paymentId(payment.getPaymentId())
                                .paymentMethod(payment.getPaymentMethod())
                                .paymentStatus(payment.getStatus())
                                .paymentAmount(payment.getAmount())
                                .build())
                        .build()
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> changeOrderStatus(Long orderId, Integer isConfirm) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (isConfirm == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<String>builder()
                                .message(MessageUtils.getMessage("common.failed"))
                                .build());
            }

            // If the order has a Stripe payment, do not allow status change
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            if (payments != null && !payments.isEmpty()) {
                boolean hasStripe = payments.stream()
                        .anyMatch(p -> p.getPaymentMethod() != null &&
                                p.getPaymentMethod().equals(Constants.CheckoutMethod.STRIPE));
                if (hasStripe) {
                    String msg = MessageUtils.getMessage("order.change.status.paid");
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.<String>builder()
                                    .message(msg)
                                    .build());
                }
            }

            switch (isConfirm) {
                case Constants.OrderAction.TO_SUCCESS -> order.setStatus(Constants.OrderStatus.SUCCESS);
                case Constants.OrderAction.TO_PENDING -> order.setStatus(Constants.OrderStatus.PENDING);
                case Constants.OrderAction.TO_CANCELLED -> order.setStatus(Constants.OrderStatus.CANCELLED);
                default -> {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.<String>builder()
                                    .message("Invalid isConfirm code. Allowed: 0(pending),1(success),2(cancelled)")
                                    .build());
                }
            }

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Order status updated successfully")
                            .result("ok")
                            .build()
            );
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .message("Error updating order status: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders
            (Long userId, int page, int size, Boolean isAdmin, String recipient) {
        Page<OrderResponse> responses = null;
        try {
            List<OrderResponse> orderResponseList = new ArrayList<>();
            List<OrderItemResponse> orderItemList = new ArrayList<>();

            Pageable pageable = PageRequest.of(page, size);

            new PageImpl<>(new ArrayList<>());
            Page<Order> orders;

            if (isAdmin) {
                orders = orderRepository.findAllByOrderByCreatedAtDesc(recipient, pageable);
            } else {
                orders = orderRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
            }

            if (!ObjectUtils.isEmpty(orders)) {

                for (Order order : orders.getContent()) {
                    orderItemList.clear();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        orderItemList.add(OrderItemResponse.builder()
                                .productName(orderItem.getProduct().getName())
                                .quantity(orderItem.getQuantity())
                                .price(orderItem.getPrice())
                                .sku(orderItem.getSku())
                                .size(orderItem.getSize())
                                .imgUrl(orderItem.getProduct().getProductImages().getFirst().getImageUrl())
                                .build());
                    }
                    String orderId = DateUtil.formatToDDMMYYYYHHMMSS(order.getCreatedAt()) + "_" + order.getOrderId();
                    OrderResponse response = OrderResponse.builder()
                            .orderId(orderId)
                            .createdAt(order.getCreatedAt())
                            .locationDetail(order.getLocationDetail())
                            .totalAmount(order.getTotalAmount())
                            .status(order.getStatus())
                            .recipient(order.getRecipient())
                            .phoneNumber(order.getPhoneNumber())
                            .orderItems(orderItemList)
                            .build();
                    orderResponseList.add(response);
                }
            }

            responses = new PageImpl<>(
                    orderResponseList, pageable, orders.getTotalElements());
            return ResponseEntity.ok(
                    ApiResponse.<Page<OrderResponse>>builder()
                            .message("Get orders successfully")
                            .result(responses)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(
                ApiResponse.<Page<OrderResponse>>builder()
                        .message("Get orders successfully")
                        .result(responses)
                        .build()
        );
    }

    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(Long orderId) {
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();

        Order order = orderRepository
                .findOrderByOrderIdAndUserId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<Long> proIds = order.getOrderItems().stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> productList = productRepository.findListProductByProductId(proIds);

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(
                        Product::getProductId,
                        product -> product
                ));


        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                    .productId(orderItem.getProductId())
                    .productName(productMap.get(orderItem.getProductId()).getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .sku(orderItem.getSku())
                    .size(orderItem.getSize())
                    .imgUrl(productMap.get(orderItem.getProductId()).getProductImages().getFirst().getImageUrl())
                    .build();
            orderItemResponseList.add(orderItemResponse);
        }

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .message("Get user orders successfully")
                        .result(OrderResponse.builder()
                                .orderId(String.valueOf(order.getOrderId()))
                                .phoneNumber(order.getPhoneNumber())
                                .createdAt(order.getCreatedAt())
                                .locationDetail(order.getLocationDetail())
                                .recipient(order.getRecipient())
                                .totalAmount(order.getTotalAmount())
                                .subtotal(order.getSubtotal())
                                .discount(order.getDiscount())
                                .status(order.getStatus())
                                .orderItems(orderItemResponseList)
                                .build())
                        .build()
        );
    }
}
