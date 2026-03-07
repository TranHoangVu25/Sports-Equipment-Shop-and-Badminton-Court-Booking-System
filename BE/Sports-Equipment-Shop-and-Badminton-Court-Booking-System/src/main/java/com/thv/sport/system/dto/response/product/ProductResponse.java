package com.thv.sport.system.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for product response")
public class ProductResponse {

    @Schema(description = "Product ID")
    private Long productId;

    @Schema(description = "Product name")
    private String name;

    @Schema(description = "Product price")
    private BigDecimal price;

    @Schema(description = "Price currency")
    private String priceCurrency;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Product quantity")
    private Integer quantity;

    @Schema(description = "Product status")
    private String status;

    @Schema(description = "Main category")
    private String mainCategory;

    @Schema(description = "Sub category")
    private String subCategory;

    @Schema(description = "Date published")
    private LocalDateTime datePublished;

    @Schema(description = "Last updated date")
    private LocalDateTime updatedAt;

    @Schema(description = "Product images list")
    private List<ProductImageResponse> productImages;
}

