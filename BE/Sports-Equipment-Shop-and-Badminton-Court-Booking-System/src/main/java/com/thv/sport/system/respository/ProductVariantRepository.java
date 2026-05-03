package com.thv.sport.system.respository;

import com.thv.sport.system.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("select pv from ProductVariant pv where pv.sku in :ids")
    List<ProductVariant> findBySku(@Param("ids") List<String> skus);

    @Modifying
    @Transactional
    @Query("delete from ProductVariant pv where pv.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}

