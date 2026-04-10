package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.SearchResultDTO;
import com.thv.sport.system.dto.response.product.ProductDocument;
import com.thv.sport.system.service.ProductSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(Constants.ApiPath.API_SEARCH)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Search Controller", description = "")
public class SearchController {

    private final ProductSearchService productSearchService;

    // Đồng bộ 1 product từ Postgres -> Elasticsearch
    @PostMapping("/sync/{productId}")
    public ProductDocument syncProduct(@PathVariable Long productId) {
        return productSearchService.syncProductToElasticsearch(productId);
    }

    // Đồng bộ tất cả product
    @PostMapping("/sync-all")
    public Iterable<ProductDocument> syncAllProducts() {
        return productSearchService.syncAllProducts();
    }

    @GetMapping()
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(value = "mainCategory", required = false) List<String> mainCategory,
            @RequestParam(value = "subCategory", required = false) List<String> subCategory,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "productId") String sortBy, //tìm kiếm theo các field id,name.keyword,createdAt
            @RequestParam(defaultValue = "desc") String sortOrder //asc, desc
    ) {
        try {
            SearchResultDTO result = productSearchService.searchProducts(
                    name,
                    mainCategory,
                    subCategory,
                    minPrice,
                    maxPrice,
                    status,
                    sizes,
                    page,
                    limit,
                    sortBy,
                    sortOrder
            );

            log.info("result: {}", result);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi trong quá trình tìm kiếm: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Yêu cầu không hợp lệ: " + e.getMessage());
        }
    }
}
