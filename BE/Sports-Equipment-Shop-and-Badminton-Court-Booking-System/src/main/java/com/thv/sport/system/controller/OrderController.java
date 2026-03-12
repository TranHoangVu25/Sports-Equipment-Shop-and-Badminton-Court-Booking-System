package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.ApiPath.API_ORDER)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout/cod")
    public ResponseEntity<ApiResponse<Order>> checkoutCOD(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.checkout(userId);
    }

    @GetMapping("get-list-order")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("get-user-list-order")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long userId = Long.valueOf(user.getUserId());
        return orderService.getOrdersByUser(userId);
    }
}