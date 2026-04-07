package com.thv.sport.system.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thv.sport.system.common.Constants;
import com.thv.sport.system.model.Order;
import com.thv.sport.system.model.OrderItem;
import com.thv.sport.system.model.Payment;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.ProductVariant;
import com.thv.sport.system.respository.OrderRepository;
import com.thv.sport.system.respository.PaymentRepository;
import com.thv.sport.system.respository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Component
public class HandleCheckoutSession {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ================= PRIVATE HANDLE =================
    @Transactional
    public void handleCheckoutSessionCompleted(String payload) {

        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode session = root.path("data").path("object");

            String paymentIntentId = session.path("payment_intent").asText();

            JsonNode metadata = session.path("metadata");
            String orderId = metadata.path("orderId").asText(null);
            String paymentId = metadata.path("paymentId").asText(null);
            long amountSubtotal = session.path("amount_subtotal").asLong(0);
            long amountTotal = session.path("amount_total").asLong(0);
            long amountDiscount = session.path("total_details").path("amount_discount").asLong(0);
            String currency = session.path("currency").asText("vnd");

            //before discount
            BigDecimal subtotal = convertStripeAmount(amountSubtotal, currency);

            //after discount
            BigDecimal totalPaid = convertStripeAmount(amountTotal, currency);

            BigDecimal discount = convertStripeAmount(amountDiscount, currency);

            Order order = orderRepository.findById(Long.valueOf(orderId))
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(Constants.OrderStatus.SUCCESS);
            order.setTotalAmount(totalPaid);
            order.setDiscount(discount);
            order.setSubtotal(subtotal);
            orderRepository.save(order);

            Payment payment = paymentRepository.findById(Long.valueOf(paymentId))
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            payment.setStatus(Constants.PaymentStatus.COMPLETED);
            payment.setProviderPaymentId(paymentIntentId);
            payment.setAmount(totalPaid);
            payment.setDiscount(discount);
            payment.setSubtotal(subtotal);
            paymentRepository.save(payment);

            //get list product id
            List<Long> productIs = order.getOrderItems()
                    .stream()
                    .map(OrderItem::getProductId)
                    .toList();

            //get product list
            List<Product> productList = productRepository.findListProductByProductId(productIs);

            //get order item map by sku
            Map<String, OrderItem> orderItemMap = order.getOrderItems().stream()
                    .collect(Collectors.toMap(
                            OrderItem::getSku,
                            Function.identity()
                    ));

            for (Product product : productList) {
                for (ProductVariant variant : product.getProductVariants()) {
                    OrderItem orderItem = orderItemMap.get(variant.getSku());
                    if (!ObjectUtils.isEmpty(orderItem)) {

                        int quantityOrdered = orderItem.getQuantity();

                        //Trừ tồn kho variant
                        variant.setQuantity(variant.getQuantity() - quantityOrdered);

                        //Trừ tồn kho product
                        product.setQuantity(product.getQuantity() - quantityOrdered);

                        //Check âm
                        if (variant.getQuantity() < 0) {
                            throw new RuntimeException("Variant out of stock: " + variant.getSku());
                        }

                        if (product.getQuantity() < 0) {
                            throw new RuntimeException("Product out of stock: " + product.getProductId());
                        }
                    }
                }
                productRepository.save(product);
            }

        } catch (Exception e) {
            log.error("Failed to process checkout.session.completed", e);
            throw new RuntimeException("Failed to process checkout.session.completed", e);
        }
    }

    // ================= MOMO HANDLE =================
    @Transactional
    public void handleMomoIpnSuccess(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            // Chỉ xử lý khi thanh toán thành công
            int resultCode = root.path("resultCode").asInt(-1);
            if (resultCode != 0) {
                log.warn("MoMo payment not successful, resultCode={}", resultCode);
                return;
            }

            String providerPaymentId = root.path("transId").asText(null); // transaction id từ MoMo
            long amountPaid = root.path("amount").asLong(0); // MoMo VND là zero-decimal
            String extraDataBase64 = root.path("extraData").asText("");

            if (ObjectUtils.isEmpty(extraDataBase64)) {
                throw new RuntimeException("MoMo extraData is empty");
            }

            // Decode extraData (Base64 JSON)
            String extraDataJson = new String(
                    Base64.getDecoder().decode(extraDataBase64),
                    StandardCharsets.UTF_8
            );

            JsonNode extraDataNode = objectMapper.readTree(extraDataJson);

            String orderId = extraDataNode.path("orderId").asText(null);
            String paymentId = extraDataNode.path("paymentId").asText(null);

            if (ObjectUtils.isEmpty(orderId) || ObjectUtils.isEmpty(paymentId)) {
                throw new RuntimeException("MoMo extraData missing orderId/paymentId");
            }

            BigDecimal totalPaid = BigDecimal.valueOf(amountPaid);

            Order order = orderRepository.findById(Long.valueOf(orderId))
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Idempotent: tránh MoMo IPN gọi lại nhiều lần
            if (Constants.OrderStatus.SUCCESS.equals(order.getStatus())) {
                log.info("MoMo order already processed, orderId={}", orderId);
                return;
            }

            // Vì MoMo không tự trả subtotal/discount chi tiết như Stripe session
            // => dùng dữ liệu đã có sẵn từ order/payment lúc checkout trước đó
            BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : totalPaid;
            BigDecimal discount = subtotal.subtract(totalPaid);

            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                discount = BigDecimal.ZERO;
            }

            order.setStatus(Constants.OrderStatus.SUCCESS);
            order.setTotalAmount(totalPaid);
            order.setDiscount(discount);
            order.setSubtotal(subtotal);
            orderRepository.save(order);

            Payment payment = paymentRepository.findById(Long.valueOf(paymentId))
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setStatus(Constants.PaymentStatus.COMPLETED);
            payment.setProviderPaymentId(providerPaymentId);
            payment.setAmount(totalPaid);
            payment.setDiscount(discount);
            payment.setSubtotal(subtotal);
            paymentRepository.save(payment);

            deductStock(order);

            log.info("MoMo payment processed successfully, orderId={}, paymentId={}, transId={}",
                    orderId, paymentId, providerPaymentId);

        } catch (Exception e) {
            log.error("Failed to process MoMo IPN success", e);
            throw new RuntimeException("Failed to process MoMo IPN success", e);
        }
    }

    // ================= COMMON STOCK DEDUCT =================
    private void deductStock(Order order) {

        // get list product id
        List<Long> productIds = order.getOrderItems()
                .stream()
                .map(OrderItem::getProductId)
                .toList();

        // get product list
        List<Product> productList = productRepository.findListProductByProductId(productIds);

        // get order item map by sku
        Map<String, OrderItem> orderItemMap = order.getOrderItems().stream()
                .collect(Collectors.toMap(
                        OrderItem::getSku,
                        Function.identity()
                ));

        for (Product product : productList) {
            for (ProductVariant variant : product.getProductVariants()) {
                OrderItem orderItem = orderItemMap.get(variant.getSku());
                if (!ObjectUtils.isEmpty(orderItem)) {

                    int quantityOrdered = orderItem.getQuantity();

                    // Trừ tồn kho variant
                    variant.setQuantity(variant.getQuantity() - quantityOrdered);

                    // Trừ tồn kho product
                    product.setQuantity(product.getQuantity() - quantityOrdered);

                    // Check âm
                    if (variant.getQuantity() < 0) {
                        throw new RuntimeException("Variant out of stock: " + variant.getSku());
                    }

                    if (product.getQuantity() < 0) {
                        throw new RuntimeException("Product out of stock: " + product.getProductId());
                    }
                }
            }
            productRepository.save(product);
        }
    }

    private BigDecimal convertStripeAmount(long amount, String currency) {
        // Zero-decimal currencies của Stripe (VND, JPY, KRW...)
        List<String> zeroDecimalCurrencies = List.of(
                "bif", "clp", "djf", "gnf", "jpy", "kmf", "krw",
                "mga", "pyg", "rwf", "ugx", "vnd", "vuv", "xaf",
                "xof", "xpf"
        );

        if (zeroDecimalCurrencies.contains(currency.toLowerCase())) {
            return BigDecimal.valueOf(amount);
        }

        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
    }
}
