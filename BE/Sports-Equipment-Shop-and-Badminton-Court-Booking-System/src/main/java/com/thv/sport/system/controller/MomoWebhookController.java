package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.service.MomoCheckoutService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.ApiPath.API_WEBHOOK)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "MoMo Webhook Controller", description = "")
public class MomoWebhookController {

    private final MomoCheckoutService momoCheckoutService;

    @PostMapping("/momo")
    public ResponseEntity<String> handleMomoIpn(HttpServletRequest request) {
        return momoCheckoutService.handleMomoIpn(request);
    }
}
