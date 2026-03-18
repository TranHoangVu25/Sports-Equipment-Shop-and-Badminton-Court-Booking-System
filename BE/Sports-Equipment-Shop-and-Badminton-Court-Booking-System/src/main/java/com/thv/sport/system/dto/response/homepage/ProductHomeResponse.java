package com.thv.sport.system.dto.response.homepage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductHomeResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String imgUrl;
}
