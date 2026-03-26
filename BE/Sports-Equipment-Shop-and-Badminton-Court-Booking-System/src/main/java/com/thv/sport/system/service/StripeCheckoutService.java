package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.order.OrderRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface StripeCheckoutService {
    String createCheckoutSession(OrderRequest request, Long userId);

    ResponseEntity<String> handleStripeWebhook(HttpServletRequest request);

}
