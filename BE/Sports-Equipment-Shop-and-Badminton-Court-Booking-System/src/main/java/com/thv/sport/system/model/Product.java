package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "price_currency", length = 255, nullable = false)
    private String priceCurrency;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "main_category", length = 255, nullable = false)
    private String mainCategory;

    @Column(name = "sub_category", length = 255, nullable = false)
    private String subCategory;

    @Column(name = "date_published")
    private LocalDateTime datePublished;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

