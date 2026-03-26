package com.thv.sport.system.service.impl;

import com.thv.sport.system.dto.response.order.OrderResponse;

public interface StripeCheckoutService {
    String createCheckoutSession(OrderResponse order);
}
