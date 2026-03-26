package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.service.StripeCheckoutService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.ApiPath.API_WEBHOOK)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Webhook Controller", description = "")
public class StripeWebhookController {
    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        return stripeCheckoutService.handleStripeWebhook(request);
    }
}
