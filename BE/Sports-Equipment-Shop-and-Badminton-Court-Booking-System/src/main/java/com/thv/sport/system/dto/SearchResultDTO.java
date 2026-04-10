package com.thv.sport.system.dto;

import com.thv.sport.system.dto.response.product.ProductDocument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SearchResultDTO {

    private long total;
    private int page;
    private int limit;
    private String status;
    private List<ProductDocument> results;
    private List<CountItemDTO> subCategory;
    private List<CountItemDTO> mainCategory;
    private List<CountItemDTO> statusCount;
}
