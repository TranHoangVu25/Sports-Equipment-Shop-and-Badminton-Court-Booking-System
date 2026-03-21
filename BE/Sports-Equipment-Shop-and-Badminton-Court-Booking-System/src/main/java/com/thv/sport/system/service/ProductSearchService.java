package com.thv.sport.system.service;

import com.thv.sport.system.dto.SearchResultDTO;
import com.thv.sport.system.dto.response.product.ProductDocument;

import java.io.IOException;
import java.util.List;

public interface ProductSearchService {
    ProductDocument syncProductToElasticsearch(Long productId);

    // Đồng bộ toàn bộ product từ Postgres sang Elasticsearch
    Iterable<ProductDocument> syncAllProducts();

    List<ProductDocument> searchByName(String keyword);

    void deleteProductFromElasticsearch(Long productId);

    SearchResultDTO searchProducts(
            String name,
            List<String> categories,
            List<String> characters,
            Double minPrice,
            Double maxPrice,
            String status,
            int page,
            int limit,
            String sortBy,
            String sortOrder
    ) throws IOException;
}
