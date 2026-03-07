package com.thv.sport.system.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO for product image response")
public class ProductImageResponse {

    @Schema(description = "Product ID (Image ID)")
    private Long productId;

    @Schema(description = "Image URL")
    private String imageUrl;
}

