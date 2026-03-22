package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.request.product.ProductCreateRequest;
import com.thv.sport.system.dto.request.product.ProductVariantRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.homepage.ProductHomeResponse;
import com.thv.sport.system.dto.response.product.ProductDetailResponse;
import com.thv.sport.system.dto.response.product.ProductResponse;
import com.thv.sport.system.dto.response.product.BatchProductResponse;
import com.thv.sport.system.dto.response.product.ProductImageResponse;
import com.thv.sport.system.dto.response.product.ProductSizeResponse;
import com.thv.sport.system.dto.response.product.VariantDTO;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.ProductImage;
import com.thv.sport.system.model.ProductVariant;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.respository.ProductImageRepository;
import com.thv.sport.system.respository.ProductVariantRepository;
import com.thv.sport.system.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ProductVariantRepository productVariantRepository;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        return null;
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
        return null;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return List.of();
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductCreateRequest request) {
        return null;
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
//    private ProductResponse buildProductResponse(Product product, List<ProductImage> productImages) {
//        ProductResponse response = ProductResponse.builder()
//                .productId(product.getProductId())
//                .name(product.getName())
//                .price(product.getPrice())
//                .priceCurrency(product.getPriceCurrency())
//                .description(product.getDescription())
//                .quantity(product.getQuantity())
//                .status(product.getStatus())
//                .mainCategory(product.getMainCategory())
//                .subCategory(product.getSubCategory())
//                .datePublished(String.valueOf(product.getDatePublished()))
////                .updatedAt(product.getUpdatedAt())
//                .build();
//
//        if (productImages != null && !productImages.isEmpty()) {
//            List<ProductImageResponse> imageResponses = new ArrayList<>();
//            for (ProductImage productImage : productImages) {
//                imageResponses.add(new ProductImageResponse(
//                        productImage.getProduct() != null ? productImage.getProduct().getProductId() : null,
//                        productImage.getImageUrl()
//                ));
//            }
//            response.setProductImages(imageResponses);
//        }
//
//        return response;
//    }

    @Override
    public ResponseEntity<ApiResponse<List<ProductHomeResponse>>> getTop10ProductsByMainCategory(String mainCategory) {
        List<Object[]> results = productRepository.findTop10ProductsByMainCategory(mainCategory);

        List<ProductHomeResponse> response = results.stream()
                .map(row -> ProductHomeResponse.builder()
                        .productId(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .price((BigDecimal) row[2])
                        .imgUrl((String) row[3])
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.<List<ProductHomeResponse>>builder()
                        .code(1000)
                        .message("Lấy danh sách sản phẩm thành công")
                        .result(response)
                        .build()
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<List<ProductResponse>>> addBatchProduct(List<ProductCreateRequest> requests) {

        List<ProductResponse> responses = new ArrayList<>();

        for (ProductCreateRequest request : requests) {

            // 1. Tạo product
            Product product = Product.builder()
                    .productId(request.getId())
                    .name(request.getName())
                    .price(request.getPrice())
                    .priceCurrency(request.getPriceCurrency())
                    .description(request.getDescription())
                    .quantity(request.getQuantity())
                    .status(getStockStatus(request.getQuantity())) // nên tự set theo quantity
                    .brand(request.getBrand())
                    .mainCategory(request.getMainCategory())
                    .subCategory(request.getSubCategory())
                    .build();

            // save product trước để có product_id
            Product savedProduct = productRepository.save(product);

            // 2. Lưu product_image
            List<String> savedImages = new ArrayList<>();
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                List<ProductImage> imgList = request.getImages().stream()
                        .filter(url -> url != null && !url.isBlank())
                        .distinct()
                        .map(url -> ProductImage.builder()
                                .imageUrl(url)
                                .product(savedProduct)
                                .build())
                        .toList();

                productImageRepository.saveAll(imgList);

                savedImages = imgList.stream()
                        .map(ProductImage::getImageUrl)
                        .toList();
            }

            // 3. Lưu product_variant từ field "size"
            String sizeType = detectSizeType(request.getMainCategory());

            List<ProductSizeResponse> sizeResponses = new ArrayList<>();

            if (request.getSize() != null && !request.getSize().isEmpty()) {
                List<ProductVariant> variants = request.getSize().stream()
                        .filter(sizeReq -> sizeReq.getSize() != null && !sizeReq.getSize().isBlank())
                        .map(sizeReq -> ProductVariant.builder()
                                .product(savedProduct)
                                .sku(generateSku(savedProduct.getProductId(), sizeType, sizeReq.getSize()))
                                .sizeValue(sizeReq.getSize())
                                .sizeType(sizeType)
                                .quantity(sizeReq.getQuantity())
                                .status(getStockStatus(sizeReq.getQuantity()))
                                .build())
                        .toList();

                productVariantRepository.saveAll(variants);

                sizeResponses = variants.stream()
                        .map(v -> ProductSizeResponse.builder()
                                .size(v.getSizeValue())
                                .quantity(v.getQuantity())
                                .build())
                        .toList();

            } else {
                ProductVariant defaultVariant = ProductVariant.builder()
                        .product(savedProduct)
                        .sku(generateSku(savedProduct.getProductId(), Constants.SizeType.NONE, "DEFAULT"))
                        .sizeValue("DEFAULT")
                        .sizeType(Constants.SizeType.NONE)
                        .quantity(request.getQuantity())
                        .status(getStockStatus(request.getQuantity()))
                        .build();

                productVariantRepository.save(defaultVariant);

                sizeResponses = List.of(
                        ProductSizeResponse.builder()
                                .size("DEFAULT")
                                .quantity(defaultVariant.getQuantity())
                                .build()
                );
            }

            // 4. Build response cho từng product
            ProductResponse response = ProductResponse.builder()
                    .productId(savedProduct.getProductId())
                    .name(savedProduct.getName())
                    .price(savedProduct.getPrice())
                    .priceCurrency(savedProduct.getPriceCurrency())
                    .description(savedProduct.getDescription())
                    .quantity(savedProduct.getQuantity())
                    .status(savedProduct.getStatus())
                    .brand(savedProduct.getBrand())
                    .mainCategory(savedProduct.getMainCategory())
                    .subCategory(savedProduct.getSubCategory())
                    .images(savedImages)
                    .sizes(sizeResponses)
                    .colors(request.getColors() != null ? request.getColors() : new ArrayList<>())
                    .build();

            responses.add(response);
        }

        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .code(200)
                        .message("Create batch products successfully")
                        .result(responses)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(Long id) {
        Product p = productRepository.findProductDetail(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDetailResponse productDetailResponse = mapToDTO(p);
        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponse>builder()
                        .code(200)
                        .message("Create batch products successfully")
                        .result(productDetailResponse)
                        .build());
    }

    private String generateSku(Long productId, String sizeType, String sizeValue) {
        return productId + "-" + sizeType + "-" + sizeValue.replaceAll("\\s+", "").toUpperCase();
    }
    private String detectSizeType(String mainCategory) {
        if (mainCategory == null || mainCategory.isBlank()) {
            return Constants.SizeType.NONE;
        }

        String category = mainCategory.trim();

        // ===== APPAREL =====
        if (category.equalsIgnoreCase("Áo Cầu Lông")
                || category.equalsIgnoreCase("Áo Pickleball")
                || category.equalsIgnoreCase("Áo tennis")
                || category.equalsIgnoreCase("Quần Cầu Lông")
                || category.equalsIgnoreCase("Quần Pickleball")
                || category.equalsIgnoreCase("Quần tennis")
                || category.equalsIgnoreCase("Váy cầu lông")
                || category.equalsIgnoreCase("Váy Pickleball")
                || category.equalsIgnoreCase("Chân Váy Tennis")
                || category.equalsIgnoreCase("Mũ")) {
            return Constants.SizeType.APPAREL;
        }

        // ===== SHOE =====
        if (category.equalsIgnoreCase("Giày Cầu Lông")
                || category.equalsIgnoreCase("Giày Cầu Lông Lining AYTM 067-1")
                || category.equalsIgnoreCase("Giày Pickleball")
                || category.equalsIgnoreCase("Giày Running")
                || category.equalsIgnoreCase("Giày Tennis")) {
            return Constants.SizeType.SHOE;
        }

        // ===== RACKET =====
        if (category.equalsIgnoreCase("Vợt Cầu Lông")
                || category.equalsIgnoreCase("Vợt PickleBall")
                || category.equalsIgnoreCase("Vợt Tennis")) {
            return Constants.SizeType.RACKET;
        }

        // ===== NONE =====
        return Constants.SizeType.NONE;
    }

    private String getStockStatus(Integer quantity) {
        return (quantity != null && quantity > 0) ? "còn hàng" : "hết hàng";
    }

    public ProductDetailResponse mapToDTO(Product p) {

        return ProductDetailResponse.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .quantity(Long.valueOf(p.getQuantity()))
                .price(p.getPrice())
                .description(p.getDescription())
                .status(p.getStatus())
                .brand(p.getBrand())

                .images(
                        p.getProductImages().stream()
                                .map(ProductImage::getImageUrl)
                                .toList()
                )

                .variants(
                        p.getProductVariants().stream()
                                .map(v -> VariantDTO.builder()
                                        .size(v.getSizeValue())
                                        .type(v.getSizeType())
                                        .quantity(v.getQuantity())
                                        .sku(v.getSku())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}

