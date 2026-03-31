package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.dto.response.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<ApiResponse<CheckoutResponse>> checkout(Long userId, OrderRequest request);

    ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(Long userId, int page, int size);

    ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(Long orderId, Long userId);

    }
