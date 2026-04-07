package com.thv.sport.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thv.sport.system.config.MomoConfig;
import com.thv.sport.system.dto.request.order.MomoCreatePaymentRequest;
import com.thv.sport.system.dto.request.order.MomoIpnRequest;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.dto.response.order.MomoCreatePaymentResponse;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.model.Payment;
import com.thv.sport.system.respository.OrderRepository;
import com.thv.sport.system.respository.PaymentRepository;
import com.thv.sport.system.service.MomoCheckoutService;
import com.thv.sport.system.util.HandleCheckoutSession;
import com.thv.sport.system.util.MomoSignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoCheckoutServiceImpl implements MomoCheckoutService {

    private final MomoConfig momoConfig;
    private final OrderServiceImpl orderServiceImpl;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final HandleCheckoutSession handleCheckoutSession;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    @Override
    public String createMomoPayment(OrderRequest request, Long userId) {

        // 1. Tạo order + payment giống flow Stripe hiện tại
        CheckoutResponse response = orderServiceImpl.checkout(userId, request).getBody().getResult();
        Long paymentId = response.getPaymentId();
        Long orderIdDb = response.getOrderId();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Order order = orderRepository.findById(orderIdDb)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Nếu payment amount chưa có thì fallback từ order
        BigDecimal paymentAmount = payment.getAmount() != null ? payment.getAmount() : order.getTotalAmount();
        if (paymentAmount == null) {
            throw new RuntimeException("Payment amount is null");
        }

        // 3. MoMo orderId unique
        String momoOrderId = "ORDER_" + order.getOrderId() + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        // 4. Dùng extraData để mang metadata giống Stripe metadata
        String extraDataJson = String.format(
                "{\"paymentId\":%d,\"orderId\":%d,\"userId\":%d}",
                payment.getPaymentId(),
                order.getOrderId(),
                userId
        );

        String extraDataBase64 = Base64.getEncoder()
                .encodeToString(extraDataJson.getBytes(StandardCharsets.UTF_8));

        // MoMo VND không có số lẻ
        String amount = String.valueOf(paymentAmount.longValue());

        String orderInfo = "Thanh toan don hang #" + order.getOrderId();

        // 5. Raw data đúng thứ tự theo create payment API
        String rawData = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraDataBase64
                + "&ipnUrl=" + momoConfig.getIpnUrl()
                + "&orderId=" + momoOrderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&redirectUrl=" + momoConfig.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + momoConfig.getRequestType();

        String signature = MomoSignatureUtil.signHmacSHA256(rawData, momoConfig.getSecretKey());

        MomoCreatePaymentRequest momoRequest = MomoCreatePaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .accessKey(momoConfig.getAccessKey())
                .requestId(requestId)
                .amount(amount)
                .orderId(momoOrderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoConfig.getRedirectUrl())
                .ipnUrl(momoConfig.getIpnUrl())
                .requestType(momoConfig.getRequestType())
                .extraData(extraDataBase64)
                .lang(momoConfig.getLang())
                .signature(signature)
                .build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MomoCreatePaymentRequest> entity = new HttpEntity<>(momoRequest, headers);

            ResponseEntity<MomoCreatePaymentResponse> momoResponse = restTemplate.exchange(
                    momoConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    MomoCreatePaymentResponse.class
            );

            MomoCreatePaymentResponse body = momoResponse.getBody();

            if (body == null) {
                throw new RuntimeException("MoMo response is null");
            }

            if (body.getResultCode() != null && body.getResultCode() != 0) {
                log.error("MoMo create payment failed: resultCode={}, message={}",
                        body.getResultCode(), body.getMessage());
                throw new RuntimeException("MoMo create payment failed: " + body.getMessage());
            }

            log.info("MoMo payment created successfully: momoOrderId={}, payUrl={}", momoOrderId, body.getPayUrl());

            return body.getPayUrl();

        } catch (Exception e) {
            log.error("MoMo checkout error", e);
            throw new RuntimeException("Failed to create MoMo payment", e);
        }
    }

    @Override
    public ResponseEntity<String> handleMomoIpn(HttpServletRequest request) {

        final String payload;
        try {
            payload = StreamUtils.copyToString(
                    request.getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            log.error("Cannot read MoMo IPN payload", e);
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            MomoIpnRequest ipn = objectMapper.readValue(payload, MomoIpnRequest.class);

            // 1. Verify signature
            String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                    + "&amount=" + ipn.getAmount()
                    + "&extraData=" + safe(ipn.getExtraData())
                    + "&message=" + safe(ipn.getMessage())
                    + "&orderId=" + ipn.getOrderId()
                    + "&orderInfo=" + safe(ipn.getOrderInfo())
                    + "&orderType=" + safe(ipn.getOrderType())
                    + "&partnerCode=" + ipn.getPartnerCode()
                    + "&payType=" + safe(ipn.getPayType())
                    + "&requestId=" + ipn.getRequestId()
                    + "&responseTime=" + ipn.getResponseTime()
                    + "&resultCode=" + ipn.getResultCode()
                    + "&transId=" + ipn.getTransId();

            String expectedSignature = MomoSignatureUtil.signHmacSHA256(rawSignature, momoConfig.getSecretKey());

            if (!expectedSignature.equals(ipn.getSignature())) {
                log.warn("Invalid MoMo signature for orderId={}", ipn.getOrderId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            log.info("MoMo IPN received: orderId={}, resultCode={}, transId={}",
                    ipn.getOrderId(), ipn.getResultCode(), ipn.getTransId());

            // 2. Thành công -> convert payload MoMo thành fake Stripe payload để reuse HandleCheckoutSession
            if (ipn.getResultCode() != null && ipn.getResultCode() == 0) {

                String extraDataJson = new String(
                        Base64.getDecoder().decode(safe(ipn.getExtraData())),
                        StandardCharsets.UTF_8
                );

                JsonNode extraDataNode = objectMapper.readTree(extraDataJson);

                String paymentId = extraDataNode.path("paymentId").asText(null);
                String orderId = extraDataNode.path("orderId").asText(null);

                if (paymentId == null || orderId == null) {
                    throw new RuntimeException("Invalid MoMo extraData: missing paymentId/orderId");
                }

                // Fake payload giống structure Stripe checkout.session.completed
                String fakeStripeLikePayload = buildFakeStripePayload(ipn, paymentId, orderId);

                handleCheckoutSession.handleCheckoutSessionCompleted(fakeStripeLikePayload);

                log.info("MoMo payment processed successfully for orderId={}, paymentId={}", orderId, paymentId);

            } else {
                log.warn("MoMo payment failed: orderId={}, resultCode={}, message={}",
                        ipn.getOrderId(), ipn.getResultCode(), ipn.getMessage());

                // TODO: nếu muốn có thể update order/payment sang FAILED ở đây
            }

            return ResponseEntity.ok("IPN processed");

        } catch (Exception e) {
            log.error("Error while handling MoMo IPN", e);
            return ResponseEntity.badRequest().body("IPN error");
        }
    }

    private String buildFakeStripePayload(MomoIpnRequest ipn, String paymentId, String orderId) {
        try {
            long amount = ipn.getAmount();

            ObjectNode metadataNode = objectMapper.createObjectNode();
            metadataNode.put("orderId", orderId);
            metadataNode.put("paymentId", paymentId);

            ObjectNode totalDetailsNode = objectMapper.createObjectNode();
            totalDetailsNode.put("amount_discount", 0);

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("payment_intent", String.valueOf(ipn.getTransId()));
            objectNode.put("amount_subtotal", amount);
            objectNode.put("amount_total", amount);
            objectNode.put("currency", "vnd");
            objectNode.set("total_details", totalDetailsNode);
            objectNode.set("metadata", metadataNode);

            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("object", objectNode);

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("type", "checkout.session.completed");
            rootNode.set("data", dataNode);

            return rootNode.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build fake Stripe payload from MoMo IPN", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}