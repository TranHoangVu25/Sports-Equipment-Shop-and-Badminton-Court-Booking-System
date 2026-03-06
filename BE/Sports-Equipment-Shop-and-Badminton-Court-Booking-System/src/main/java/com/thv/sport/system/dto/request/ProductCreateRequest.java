package com.thv.sport.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new product with images")
public class ProductCreateRequest {

    @Schema(description = "Product ID (optional, auto-generated if not provided)", example = "24189")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Set băng chặn mồ hôi Yonex 11510-2 chính hãng")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Schema(description = "Product price", example = "99000")
    private BigDecimal price;

    @NotBlank(message = "Price currency is required")
    @Schema(description = "Price currency", example = "VND")
    private String priceCurrency;

    @Schema(description = "Product description", example = "Set băng chặn mồ hôi chính hãng")
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    @Schema(description = "Product quantity", example = "294")
    private Integer quantity;

    @NotBlank(message = "Status is required")
    @Schema(description = "Product status", example = "còn hàng")
    private String status;

    @NotBlank(message = "Main category is required")
    @Schema(description = "Main product category", example = "Phụ Kiện Cầu Lông")
    private String mainCategory;

    @NotBlank(message = "Sub category is required")
    @Schema(description = "Sub product category", example = "Băng chặn mồ hôi")
    private String subCategory;

    @Schema(description = "List of product image URLs")
    private List<String> images;
}

