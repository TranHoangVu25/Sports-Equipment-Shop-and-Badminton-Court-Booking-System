package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.request.booking.BookingRequest;
import com.thv.sport.system.service.BookingService;
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

    private final BookingService bookingService;

    @PostMapping("/momo")
    public ResponseEntity<String> createMomoPayment(@RequestBody BookingRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        Long userId = Long.valueOf(user.getUserId());

        String payUrl = bookingService.checkoutBooking(userId, request);
        return ResponseEntity.ok(payUrl);
    }
}
