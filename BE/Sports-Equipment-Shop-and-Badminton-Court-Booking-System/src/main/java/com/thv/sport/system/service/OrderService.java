package com.thv.sport.system.service;

import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.model.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<ApiResponse<Order>> checkout(Long userId);

    ResponseEntity<ApiResponse<List<Order>>> getAllOrders();

    ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(Long userId);
}
