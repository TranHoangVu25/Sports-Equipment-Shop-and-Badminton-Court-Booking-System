package com.thv.sport.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for batch product response")
public class BatchProductResponse {

    @Schema(description = "Total number of products processed")
    private Integer total;

    @Schema(description = "Number of products successfully created")
    private Integer success;

    @Schema(description = "Number of products failed to create")
    private Integer failed;

    @Schema(description = "List of successfully created products")
    private List<ProductResponse> products;

    @Schema(description = "List of error messages for failed products")
    private List<String> errors;
}

