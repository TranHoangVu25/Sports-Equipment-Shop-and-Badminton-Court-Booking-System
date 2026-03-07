package com.thv.sport.system.service.impl;

import com.thv.sport.system.dto.request.ProductCreateRequest;
import com.thv.sport.system.dto.response.product.ProductResponse;
import com.thv.sport.system.dto.response.product.BatchProductResponse;
import com.thv.sport.system.dto.response.product.ProductImageResponse;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.ProductImage;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.respository.ProductImageRepository;
import com.thv.sport.system.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IProductService interface
 * Handles all product-related business logic
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = buildProductEntity(request);
        Product savedProduct = productRepository.save(product);

        List<ProductImage> productImages = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String imageUrl : request.getImages()) {
                ProductImage productImage = createProductImage(savedProduct, imageUrl);
                productImage = productImageRepository.save(productImage);
                productImages.add(productImage);
            }
        }

        return buildProductResponse(savedProduct, productImages);
    }

    @Override
    @Transactional
    public BatchProductResponse createProductsBatch(List<ProductCreateRequest> requests) {
        List<ProductResponse> successProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCount = requests.size();

        for (int index = 0; index < requests.size(); index++) {
            ProductCreateRequest request = requests.get(index);
            try {
                ProductResponse response = createProduct(request);
                successProducts.add(response);
            } catch (Exception e) {
                String errorMessage = String.format(
                    "Product %d (%s): %s",
                    index + 1,
                    request.getName(),
                    e.getMessage()
                );
                errors.add(errorMessage);
            }
        }

        return BatchProductResponse.builder()
                .total(totalCount)
                .success(successProducts.size())
                .failed(errors.size())
                .products(successProducts)
                .errors(errors)
                .build();
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    List<ProductImage> images = productImageRepository.findAll().stream()
                            .filter(img -> img.getProduct().getProductId().equals(productId))
                            .toList();
                    return buildProductResponse(product, images);
                })
                .orElse(null);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> responses = new ArrayList<>();

        for (Product product : products) {
            List<ProductImage> images = productImageRepository.findAll().stream()
                    .filter(img -> img.getProduct().getProductId().equals(product.getProductId()))
                    .toList();
            responses.add(buildProductResponse(product, images));
        }

        return responses;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductCreateRequest request) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setName(request.getName());
                    product.setPrice(request.getPrice());
                    product.setPriceCurrency(request.getPriceCurrency());
                    product.setDescription(request.getDescription());
                    product.setQuantity(request.getQuantity());
                    product.setStatus(request.getStatus());
                    product.setMainCategory(request.getMainCategory());
                    product.setSubCategory(request.getSubCategory());
                    product.setUpdatedAt(LocalDateTime.now());

                    Product updatedProduct = productRepository.save(product);

                    if (request.getImages() != null && !request.getImages().isEmpty()) {
                        productImageRepository.deleteAll(product.getProductImages());

                        List<ProductImage> newImages = new ArrayList<>();
                        for (String imageUrl : request.getImages()) {
                            ProductImage productImage = createProductImage(updatedProduct, imageUrl);
                            productImage = productImageRepository.save(productImage);
                            newImages.add(productImage);
                        }
                        updatedProduct.setProductImages(newImages);
                    }

                    return buildProductResponse(updatedProduct, updatedProduct.getProductImages());
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    productRepository.delete(product);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public BatchProductResponse deleteProductsBatch(List<Long> productIds) {
        List<String> deletedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCount = productIds.size();

        for (int index = 0; index < productIds.size(); index++) {
            Long productId = productIds.get(index);
            try {
                if (deleteProduct(productId)) {
                    deletedIds.add("Product ID: " + productId);
                } else {
                    errors.add(String.format("Product %d (ID: %d): Not found", index + 1, productId));
                }
            } catch (Exception e) {
                String errorMessage = String.format(
                    "Product %d (ID: %d): %s",
                    index + 1,
                    productId,
                    e.getMessage()
                );
                errors.add(errorMessage);
            }
        }

        return BatchProductResponse.builder()
                .total(totalCount)
                .success(deletedIds.size())
                .failed(errors.size())
                .products(new ArrayList<>())
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public boolean deleteProductImage(Long imageId) {
        return productImageRepository.findById(imageId)
                .map(image -> {
                    productImageRepository.delete(image);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public BatchProductResponse deleteProductImagesBatch(List<Long> imageIds) {
        List<String> deletedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCount = imageIds.size();

        for (int index = 0; index < imageIds.size(); index++) {
            Long imageId = imageIds.get(index);
            try {
                if (deleteProductImage(imageId)) {
                    deletedIds.add("Image ID: " + imageId);
                } else {
                    errors.add(String.format("Image %d (ID: %d): Not found", index + 1, imageId));
                }
            } catch (Exception e) {
                String errorMessage = String.format(
                    "Image %d (ID: %d): %s",
                    index + 1,
                    imageId,
                    e.getMessage()
                );
                errors.add(errorMessage);
            }
        }

        return BatchProductResponse.builder()
                .total(totalCount)
                .success(deletedIds.size())
                .failed(errors.size())
                .products(new ArrayList<>())
                .errors(errors)
                .build();
    }

    /**
     * Build Product entity from request
     */
    private Product buildProductEntity(ProductCreateRequest request) {
        Product product = new Product();
        if (request.getId() != null) {
            product.setProductId(request.getId());
        }
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setPriceCurrency(request.getPriceCurrency());
        product.setDescription(request.getDescription());
        product.setQuantity(request.getQuantity());
        product.setStatus(request.getStatus());
        product.setMainCategory(request.getMainCategory());
        product.setSubCategory(request.getSubCategory());
        product.setDatePublished(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    /**
     * Create ProductImage entity linked to Product
     */
    private ProductImage createProductImage(Product product, String imageUrl) {
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageUrl);
        return productImage;
    }

    /**
     * Build ProductResponse from Product entity
     */
    private ProductResponse buildProductResponse(Product product, List<ProductImage> productImages) {
        ProductResponse response = ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .priceCurrency(product.getPriceCurrency())
                .description(product.getDescription())
                .quantity(product.getQuantity())
                .status(product.getStatus())
                .mainCategory(product.getMainCategory())
                .subCategory(product.getSubCategory())
                .datePublished(product.getDatePublished())
                .updatedAt(product.getUpdatedAt())
                .build();

        if (productImages != null && !productImages.isEmpty()) {
            List<ProductImageResponse> imageResponses = new ArrayList<>();
            for (ProductImage productImage : productImages) {
                imageResponses.add(new ProductImageResponse(
                        productImage.getProduct() != null ? productImage.getProduct().getProductId() : null,
                        productImage.getImageUrl()
                ));
            }
            response.setProductImages(imageResponses);
        }

        return response;
    }
}

