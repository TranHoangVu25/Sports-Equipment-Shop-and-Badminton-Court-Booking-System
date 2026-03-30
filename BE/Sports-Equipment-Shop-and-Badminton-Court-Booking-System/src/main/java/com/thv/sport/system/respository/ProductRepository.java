package com.thv.sport.system.respository;

import com.thv.sport.system.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
    SELECT 
        p.product_id,
        p.name,
        p.price,
        MIN(pi.image_url) AS img_url
    FROM product p
    LEFT JOIN product_image pi ON p.product_id = pi.product_id
    WHERE p.main_category = :mainCategory
    GROUP BY p.product_id, p.name, p.price
    ORDER BY p.product_id ASC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findTop10ProductsByMainCategory(@Param("mainCategory") String mainCategory);

    @Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN  p.productImages pi
    LEFT JOIN  p.productVariants pv
    WHERE p.productId = :productId
""")
    Optional<Product> findProductDetail(@Param("productId") Long productId);

    @Query("select p from Product p where p.productId in :ids")
    List<Product> findListProductByProductId(@Param("ids") List<Long> productId);

    Page<Product> findAll(Pageable pageable);
}

