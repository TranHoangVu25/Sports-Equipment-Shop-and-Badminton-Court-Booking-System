package com.thv.sport.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.thv.sport.system.dto.request.order.OrderRequest;
import com.thv.sport.system.dto.response.order.CheckoutResponse;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.model.Payment;
import com.thv.sport.system.respository.OrderRepository;
import com.thv.sport.system.respository.PaymentRepository;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.respository.ProductVariantRepository;
import com.thv.sport.system.service.StripeCheckoutService;
import com.thv.sport.system.util.HandleCheckoutSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutServiceImpl implements StripeCheckoutService {
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final OrderServiceImpl orderServiceImpl;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final HandleCheckoutSession handleCheckoutSessionCompleted;

    public static final String SIZE_DEFAULT = "DEFAULT";

    @Transactional
    @Override
    public String createCheckoutSession(OrderRequest request, Long userId) {

        CheckoutResponse response = orderServiceImpl.checkout(userId, request).getBody().getResult();
        Long paymentId = response.getPaymentId();
        Long orderId = response.getOrderId();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        SessionCreateParams.Builder params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setLocale(SessionCreateParams.Locale.EN)
                        .setSuccessUrl("http://localhost:5173/profile")
                        .setCancelUrl("http://localhost:5173/payment/failed")
                        .setAllowPromotionCodes(true)
                        .putMetadata("orderId", String.valueOf(order.getOrderId()))
                        .putMetadata("paymentId", String.valueOf(payment.getPaymentId()))
                        .putMetadata("totalPrice", String.valueOf(payment.getAmount()))
                        .putMetadata("userId", String.valueOf(userId));

        BigDecimal totalVnd = BigDecimal.ZERO;

        for (var item : order.getOrderItems()) {

            BigDecimal priceVnd = item.getPrice();

            // Tổng tiền
            BigDecimal itemTotal = priceVnd.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalVnd = totalVnd.add(itemTotal);

            long unitAmount = priceVnd.longValue();

            String size = " ";
            if (!item.getSize().equals(SIZE_DEFAULT)) {
                size = "Size: " + item.getSize();
            }

            params.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("vnd")
                                            .setUnitAmount(unitAmount)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getProduct().getName())
                                                            .setDescription(size)
                                                            .addImage(item.getProduct().getProductImages()
                                                                    .getFirst().getImageUrl())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        params.putMetadata("totalAmountVnd", totalVnd.toPlainString());

        try {
            Session session = Session.create(params.build());
            return session.getUrl();
        } catch (Exception e) {
            log.error("Stripe checkout error", e);
            throw new RuntimeException("Failed to create Stripe Checkout Session", e);
        }
    }

    @Override
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {

        final String payload;
        try {
            payload = StreamUtils.copyToString(
                    request.getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            log.error("Cannot read webhook payload", e);
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        final String sigHeader = request.getHeader("Stripe-Signature");
        if (sigHeader == null) {
            log.warn("Missing Stripe-Signature header");
            return ResponseEntity.badRequest().body("Missing signature");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid signature");
        } catch (Exception e) {
            log.error("Stripe webhook verification error", e);
            return ResponseEntity.badRequest().body("Webhook error");
        }

        log.info("Stripe webhook received: type={}, id={}",
                event.getType(), event.getId());

        try {
            if ("checkout.session.completed".equals(event.getType())) {
                handleCheckoutSessionCompleted.handleCheckoutSessionCompleted(payload);
            } else {
                log.info("Ignore Stripe event type={}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error while handling Stripe event", e);
            throw new RuntimeException("Error while handling Stripe event", e);
        }
        return ResponseEntity.ok("Webhook ok");
    }
}
