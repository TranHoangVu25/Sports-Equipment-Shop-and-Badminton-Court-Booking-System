package com.thv.sport.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Base response wrapper for all API responses")
public class BaseResponse<T> {

    @Schema(description = "HTTP status code", example = "200")
    private Integer status;

    @Schema(description = "Error message (null if successful)")
    private String error;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Message or description")
    private String message;

    @Schema(description = "Response data")
    private T data;
}

