package com.thv.sport.system.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;
    private String name;
    private Long quantity;
    private BigDecimal price;
    private String description;
    private String status;
    private String brand;

    private List<String> images;

    private List<VariantDTO> variants;
}
