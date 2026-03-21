package com.thv.sport.system.dto.request.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {
    private String sizeValue;   // M, L, 42, 4U5
    private String sizeType;    // APPAREL, SHOE, RACKET, NONE
    private Integer quantity;
    private String sku;         // optional
    private String status;      // optional
}