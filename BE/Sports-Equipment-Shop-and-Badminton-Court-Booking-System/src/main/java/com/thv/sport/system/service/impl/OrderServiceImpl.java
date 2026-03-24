package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.common.DateUtil;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.order.OrderResponse;
import com.thv.sport.system.model.Cart;
import com.thv.sport.system.model.CartItem;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.model.OrderItem;
import com.thv.sport.system.model.Payment;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.CartItemRepository;
import com.thv.sport.system.respository.CartRepository;
import com.thv.sport.system.respository.OrderRepository;
import com.thv.sport.system.respository.PaymentRepository;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OrderServiceImpl implements OrderService {
    CartRepository cartRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    PaymentRepository paymentRepository;
    CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Order>> checkout(Long userId, OrderRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
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
        payment.setPaymentMethod(Constants.CheckoutMethod.COD);
        payment.setStatus(Constants.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // 5. clear cart
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        return ResponseEntity.ok(
                ApiResponse.<Order>builder()
                        .message("Checkout COD successfully")
                        .result(savedOrder)
                        .build()
        );
    }

    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orderResponseList = new ArrayList<>();

        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

        for (Order order : orders) {
            String orderId = DateUtil.formatToDDMMYYYYHHMMSS(order.getCreatedAt()) +"_"+ order.getOrderId();
            OrderResponse response = OrderResponse.builder()
                    .orderId(orderId)
                    .createdAt(order.getCreatedAt())
                    .locationDetail(order.getLocationDetail())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .build();
            orderResponseList.add(response);
        }


        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .message("Get orders successfully")
                        .result(orderResponseList)
                        .build()
        );
    }

    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(Long userId) {

        List<Order> orders = orderRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<Order>>builder()
                        .message("Get user orders successfully")
                        .result(orders)
                        .build()
        );
    }
}
