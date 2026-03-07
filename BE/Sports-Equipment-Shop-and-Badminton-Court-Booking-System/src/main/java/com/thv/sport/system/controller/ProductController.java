package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.request.ProductCreateRequest;
import com.thv.sport.system.dto.response.BaseResponse;
import com.thv.sport.system.dto.response.product.BatchProductResponse;
import com.thv.sport.system.dto.response.product.ProductResponse;
import com.thv.sport.system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.ApiPath.API_PRODUCT)
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "APIs for managing products and product images")
public class ProductController extends BaseController {

    private final ProductService productService;

    /**
     * Create a single product with image
     *
     * @param request ProductCreateRequest containing product details and images
     * @return ResponseEntity with BaseResponse<ProductResponse> and HTTP 201 Created status
     */
    @PostMapping
    @Operation(summary = "Create a single product with image",
            description = "Creates a new product in the system along with its images. All images from the images list will be saved as the product's images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        try {
            ProductResponse response = productService.createProduct(request);
            return createdResponse(response, "SUCCESS.CREATE");
        } catch (Exception e) {
            return internalErrorResponse("Error creating product: " + e.getMessage());
        }
    }

    /**
     * Create multiple products with images in batch
     *
     * @param requests List of ProductCreateRequest containing product details and images
     * @return ResponseEntity with BaseResponse<BatchProductResponse> and HTTP 201 Created status
     */
    @PostMapping("/batch")
    @Operation(summary = "Create multiple products in batch",
            description = "Creates multiple products in the system along with their images. All images from each product's images list will be saved. Returns detailed success/failure statistics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Batch processing completed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<BatchProductResponse>> createProductsBatch(
             @RequestBody List<ProductCreateRequest> requests) {
        try {
            BatchProductResponse response = productService.createProductsBatch(requests);
            return createdResponse(response, "SUCCESS.CREATE");
        } catch (Exception e) {
            return internalErrorResponse("Error creating products batch: " + e.getMessage());
        }
    }

    /**
     * Get all products
     *
     * @return ResponseEntity with BaseResponse<List<ProductResponse>> and HTTP 200 OK status
     */
    @GetMapping
    @Operation(summary = "Get all products",
            description = "Retrieves all products from the system with their images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAllProducts() {
        try {
            List<ProductResponse> products = productService.getAllProducts();
            return successResponse(products, "SUCCESS.FETCH");
        } catch (Exception e) {
            return internalErrorResponse("Error fetching products: " + e.getMessage());
        }
    }

    /**
     * Get product by ID
     *
     * @param id the product ID
     * @return ResponseEntity with BaseResponse<ProductResponse> and HTTP 200 OK status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID",
            description = "Retrieves a single product with its images by product ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            if (product == null) {
                return notFoundResponse("Product with ID " + id + " not found");
            }
            return successResponse(product, "SUCCESS.FETCH");
        } catch (Exception e) {
            return internalErrorResponse("Error fetching product: " + e.getMessage());
        }
    }

    /**
     * Update product by ID
     *
     * @param id the product ID
     * @param request ProductCreateRequest containing updated product details
     * @return ResponseEntity with BaseResponse<ProductResponse> and HTTP 200 OK status
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product by ID",
            description = "Updates an existing product with new details and images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateRequest request) {
        try {
            ProductResponse product = productService.updateProduct(id, request);
            if (product == null) {
                return notFoundResponse("Product with ID " + id + " not found");
            }
            return successResponse(product, "SUCCESS.UPDATE");
        } catch (Exception e) {
            return internalErrorResponse("Error updating product: " + e.getMessage());
        }
    }

    /**
     * Delete product by ID
     *
     * @param id the product ID
     * @return ResponseEntity with BaseResponse<Void> and HTTP 200 OK status
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by ID",
            description = "Deletes a product and all its associated images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (!deleted) {
                return notFoundResponse("Product with ID " + id + " not found");
            }
            return successResponse("SUCCESS.DELETE");
        } catch (Exception e) {
            return internalErrorResponse("Error deleting product: " + e.getMessage());
        }
    }

    /**
     * Delete multiple products in batch
     *
     * @param productIds List of product IDs to delete
     * @return ResponseEntity with BaseResponse<BatchProductResponse> and HTTP 200 OK status
     */
    @DeleteMapping("/batch")
    @Operation(summary = "Delete multiple products in batch",
            description = "Deletes multiple products and their associated images. Returns detailed success/failure statistics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch deletion completed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<BatchProductResponse>> deleteProductsBatch(
            @RequestBody List<Long> productIds) {
        try {
            BatchProductResponse response = productService.deleteProductsBatch(productIds);
            return successResponse(response, "SUCCESS.DELETE");
        } catch (Exception e) {
            return internalErrorResponse("Error deleting products batch: " + e.getMessage());
        }
    }
}

