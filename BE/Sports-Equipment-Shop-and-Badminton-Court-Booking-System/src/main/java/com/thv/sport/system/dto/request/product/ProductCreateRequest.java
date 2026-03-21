package com.thv.sport.system.dto.request.product;

import com.thv.sport.system.dto.response.product.ProductColorResponse;
import com.thv.sport.system.dto.response.product.ProductSizeResponse;
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
@Schema(description = "DTO for creating a new product with images and variants")
public class ProductCreateRequest {

    @Schema(description = "Product ID (optional, auto-generated if not provided)", example = "20676")
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "Price currency is required")
    private String priceCurrency;

    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Main category is required")
    private String mainCategory;

    @NotBlank(message = "Sub category is required")
    private String subCategory;

    private List<String> images;

    // JSON của bạn là "size" nên field phải là size
    private List<ProductSizeResponse> size;

    private List<ProductColorResponse> colors;

    private String brand;

    private String datePublished;
}