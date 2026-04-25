package com.thv.sport.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thv.sport.system.config.MomoConfig;
import com.thv.sport.system.dto.request.order.MomoCreatePaymentRequest;
import com.thv.sport.system.dto.request.order.MomoIpnRequest;
import com.thv.sport.system.dto.response.order.MomoCreatePaymentResponse;
import com.thv.sport.system.model.Booking;
import com.thv.sport.system.model.BookingPayment;
import com.thv.sport.system.respository.BookingPaymentRepository;
import com.thv.sport.system.respository.BookingRepository;
import com.thv.sport.system.service.MomoCheckoutService;
import com.thv.sport.system.util.MomoSignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoCheckoutServiceImpl implements MomoCheckoutService {

    private final MomoConfig momoConfig;
    private final ObjectMapper objectMapper;
    private final BookingRepository bookingRepository;
    private final BookingPaymentRepository bookingPaymentRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    @Override
    public String createPayment(Booking booking, BookingPayment payment, Long userId) {
        BigDecimal amount = payment.getAmount();
        if (amount == null) {
            throw new RuntimeException("Payment amount is null");
        }

        String momoOrderId = "BOOKING_" + booking.getBookingId() + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        // metadata
        String extraDataJson = String.format(
                "{\"bookingId\":%d,\"paymentId\":%d,\"userId\":%d}",
                booking.getBookingId(),
                payment.getBookingPaymentId(),
                userId
        );

        String extraDataBase64 = Base64.getEncoder()
                .encodeToString(extraDataJson.getBytes(StandardCharsets.UTF_8));

        String amountStr = String.valueOf(amount.longValue());

        String orderInfo = "Thanh toan booking #" + booking.getBookingId();

        String rawData = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amountStr
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
                .amount(amountStr)
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

            ResponseEntity<MomoCreatePaymentResponse> response = restTemplate.exchange(
                    momoConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    MomoCreatePaymentResponse.class
            );

            MomoCreatePaymentResponse body = response.getBody();

            if (body == null || body.getResultCode() != 0) {
                throw new RuntimeException("MoMo create payment failed: " +
                        (body != null ? body.getMessage() : "null response"));
            }

            return body.getPayUrl();

        } catch (Exception e) {
            log.error("MoMo error", e);
            throw new RuntimeException("MoMo payment failed");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> handleMomoIpn(HttpServletRequest request) {

        final String payload;
        try {
            payload = StreamUtils.copyToString(
                    request.getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            MomoIpnRequest ipn = objectMapper.readValue(payload, MomoIpnRequest.class);

            // ===== verify signature =====
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

            String expected = MomoSignatureUtil.signHmacSHA256(rawSignature, momoConfig.getSecretKey());

            if (!expected.equals(ipn.getSignature())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // ===== decode metadata =====
            String extraDataJson = new String(
                    Base64.getDecoder().decode(safe(ipn.getExtraData())),
                    StandardCharsets.UTF_8
            );

            JsonNode node = objectMapper.readTree(extraDataJson);

            Integer bookingId = node.get("bookingId").asInt();
            Long paymentId = node.get("paymentId").asLong();

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            BookingPayment payment = bookingPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // ===== SUCCESS =====
            if (ipn.getResultCode() == 0) {

                payment.setStatus("SUCCESS");
                payment.setProviderPaymentId(String.valueOf(ipn.getTransId()));
                payment.setUpdatedAt(LocalDateTime.now());

                booking.setStatus("PAID");
                booking.setUpdatedAt(LocalDateTime.now());

            } else {
                payment.setStatus("FAILED");
                payment.setFailureReason(ipn.getMessage());
                payment.setUpdatedAt(LocalDateTime.now());

                booking.setStatus("CANCELLED");
                booking.setUpdatedAt(LocalDateTime.now());
            }

            bookingPaymentRepository.save(payment);
            bookingRepository.save(booking);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("IPN error", e);
            return ResponseEntity.badRequest().body("IPN error");
        }
    }
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
