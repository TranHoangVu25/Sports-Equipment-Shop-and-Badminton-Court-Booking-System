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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long productId;

    // id gốc từ nguồn crawl nếu có
    private Long sourceProductId;

    private String name;

    private BigDecimal price;

    private String priceCurrency;

    private String description;

    // tổng stock product
    private Integer quantity;

    private String status;

    private String mainCategory;

    private String subCategory;

    private String brand;

    private List<String> images;

    // giữ key "size" cho giống JSON bạn đang import / trả ra
    private List<ProductSizeResponse> sizes;

    private List<ProductColorResponse> colors;

    private String datePublished;
}