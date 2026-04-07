package com.thv.sport.system.dto.response.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoCreatePaymentResponse {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private Long amount;
    private Integer resultCode;
    private String message;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
}
