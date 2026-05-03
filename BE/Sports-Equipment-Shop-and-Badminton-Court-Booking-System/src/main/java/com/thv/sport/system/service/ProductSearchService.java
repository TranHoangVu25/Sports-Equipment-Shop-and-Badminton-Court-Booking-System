package com.thv.sport.system.service;

import com.thv.sport.system.dto.SearchResultDTO;
import com.thv.sport.system.dto.response.product.ProductDocument;
import com.thv.sport.system.model.Product;

import java.io.IOException;
import java.util.List;

public interface ProductSearchService {
    ProductDocument syncProductToElasticsearch(Long productId);

    // Đồng bộ toàn bộ product từ Postgres sang Elasticsearch
    Iterable<ProductDocument> syncAllProducts();

    ProductDocument syncProduct(Product product);

    List<ProductDocument> searchByName(String keyword);

    void deleteProductFromElasticsearch(Long productId);

    SearchResultDTO searchProducts(
            String name,
            List<String> mainCategory,
            List<String> subCategory,
            Double minPrice,
            Double maxPrice,
            String status,
            List<String> sizes,
            int page,
            int limit,
            String sortBy,
            String sortOrder
    ) throws IOException;
}
