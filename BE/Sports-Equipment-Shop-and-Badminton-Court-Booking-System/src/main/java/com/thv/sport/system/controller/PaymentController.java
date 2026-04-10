package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.service.MomoCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.ApiPath.API_ORDER)
@RequiredArgsConstructor
public class PaymentController {

    private final MomoCheckoutService momoCheckoutService;

    @PostMapping("/momo")
    public ResponseEntity<String> createMomoPayment(@RequestBody OrderRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        try {

            Long userId = Long.valueOf(user.getUserId());

            String payUrl = momoCheckoutService.createMomoPayment(request, userId);
            return ResponseEntity.ok(payUrl);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
