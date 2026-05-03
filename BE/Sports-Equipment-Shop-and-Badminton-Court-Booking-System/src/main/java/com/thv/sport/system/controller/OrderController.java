package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.dto.response.order.OrderResponse;
import com.thv.sport.system.service.OrderService;
import com.thv.sport.system.service.StripeCheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.ApiPath.API_ORDER)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/checkout/cod")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkoutCOD(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody OrderRequest request
            ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.checkout(userId, request);
    }

    @GetMapping("/get-user-list-order")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.getAllOrders(userId, page, size, false, null);
    }

    @GetMapping("/get-all-list-order")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrdersAdmin(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String recipient
    ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.getAllOrders(userId, page, size, true, recipient);
    }

    @PostMapping("/checkout-stripe-url")
    public ResponseEntity<ApiResponse<String>> createCheckout(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Integer userId = user.getUserId();
        // Build Stripe Checkout
        String checkoutUrl =
                stripeCheckoutService.createCheckoutSession(request, Long.valueOf(userId));

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<String>builder()
                                .message("Get check out URL")
                                .result(checkoutUrl)
                                .build());
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        return stripeCheckoutService.handleStripeWebhook(request);
    }


    @GetMapping("/get-order-detail/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("orderId") Long orderId
    ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.getOrderDetail(orderId);
    }

    @PutMapping("/change-order-status/{orderId}")
    public ResponseEntity<ApiResponse<String>> changeOrderStatus(
            @RequestParam Integer isConfirm,
            @PathVariable("orderId") Long orderId
    ) {
        return orderService.changeOrderStatus(orderId, isConfirm);
    }
}
