package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.ProductCreateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.homepage.ProductHomeResponse;
import com.thv.sport.system.dto.response.product.BatchProductResponse;
import com.thv.sport.system.dto.response.product.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Service interface for Product operations
 */
public interface ProductService {

    /**
     * Create a single product with images
     * @param request ProductCreateRequest DTO
     * @return ProductResponse DTO
     */
    ProductResponse createProduct(ProductCreateRequest request);

    /**
     * Create multiple products with images in batch
     * @param requests List of ProductCreateRequest DTOs
     * @return BatchProductResponse containing success/failure statistics
     */
    BatchProductResponse createProductsBatch(List<ProductCreateRequest> requests);

    /**
     * Get product by ID
     * @param productId the product ID
     * @return ProductResponse or null if not found
     */
    ProductResponse getProductById(Long productId);

    /**
     * Get all products
     * @return List of ProductResponse
     */
    List<ProductResponse> getAllProducts();

    /**
     * Update a product
     * @param productId the product ID
     * @param request ProductCreateRequest with updated data
     * @return ProductResponse or null if not found
     */
    ProductResponse updateProduct(Long productId, ProductCreateRequest request);

    /**
     * Delete a single product and its images
     * @param productId the product ID
     * @return true if deleted, false if not found
     */
    boolean deleteProduct(Long productId);

    /**
     * Delete multiple products in batch
     * @param productIds List of product IDs to delete
     * @return Batch response with success/failure statistics
     */
    BatchProductResponse deleteProductsBatch(List<Long> productIds);

    /**
     * Delete a product image
     * @param imageId the image ID
     * @return true if deleted, false if not found
     */
    boolean deleteProductImage(Long imageId);

    /**
     * Delete multiple product images in batch
     * @param imageIds List of image IDs to delete
     * @return Batch response with success/failure statistics
     */
    BatchProductResponse deleteProductImagesBatch(List<Long> imageIds);

    ResponseEntity<ApiResponse<List<ProductHomeResponse>>> getTop10ProductsByMainCategory(String mainCategory);
}

