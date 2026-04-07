package com.thv.sport.system.dto.request.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomoCreatePaymentRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private String amount;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType;
    private String extraData;
    private String lang;
    private String signature;
}
