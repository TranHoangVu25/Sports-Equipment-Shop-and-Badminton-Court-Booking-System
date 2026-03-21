package com.thv.sport.system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(name = "sku", unique = true)
    private String sku;

    // Ví dụ: M, L, XL, 39, 40, 41, 4U5, 4U6
    @Column(name = "size_value", nullable = false, length = 50)
    private String sizeValue;

    // Loại size để biết đây là áo, giày hay vợt
    // APPAREL, SHOE, RACKET
    @Column(name = "size_type", nullable = false, length = 50)
    private String sizeType;

    // Tồn kho theo biến thể
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", length = 50)
    private String status;
}