package com.thv.sport.system.dto.response.user;

import com.thv.sport.system.dto.response.order.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {
    private UserResponse userInfo;
    private List<OrderResponse> orders;
    private Double totalAmount;
    private Integer totalOrders;
}
