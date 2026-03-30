package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.dto.response.order.OrderResponse;
import com.thv.sport.system.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<ApiResponse<CheckoutResponse>> checkout(Long userId, OrderRequest request);

    ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(int page, int size);

    ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(Long userId);

    ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(Long orderId, Long userId);

    }
