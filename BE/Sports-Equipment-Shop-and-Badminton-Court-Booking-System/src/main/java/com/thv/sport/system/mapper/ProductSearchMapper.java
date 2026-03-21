package com.thv.sport.system.mapper;

import com.thv.sport.system.dto.response.product.ProductDocument;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.model.ProductImage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductSearchMapper {
    public ProductDocument toDocument(Product product) {
        List<String> imageUrls = product.getProductImages() != null
                ? product.getProductImages().stream()
                .map(ProductImage::getImageUrl) // đổi lại nếu field của em tên khác
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ProductDocument.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .priceCurrency(product.getPriceCurrency())
                .description(product.getDescription())
                .quantity(product.getQuantity())
                .status(product.getStatus())
                .brand(product.getBrand())
                .mainCategory(product.getMainCategory())
                .subCategory(product.getSubCategory())
                .datePublished(product.getDatePublished())
                .updatedAt(product.getUpdatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
