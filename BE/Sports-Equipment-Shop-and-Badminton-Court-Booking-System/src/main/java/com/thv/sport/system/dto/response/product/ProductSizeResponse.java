package com.thv.sport.system.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSizeResponse {

    private String size;       // 36, 37, M, XL, 4U5
    private Integer quantity;  // tồn kho size đó

    // optional nhưng rất nên có
    private String sizeType;   // SHOE, APPAREL, RACKET, NONE
    private String sku;        // 20676-SHOE-36
    private String status;     // ACTIVE
}