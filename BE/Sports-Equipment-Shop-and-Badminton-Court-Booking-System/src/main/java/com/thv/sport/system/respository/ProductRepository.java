package com.thv.sport.system.respository;

import com.thv.sport.system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    ORDER BY p.product_id DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findTop10ProductsByMainCategory(@Param("mainCategory") String mainCategory);
}

