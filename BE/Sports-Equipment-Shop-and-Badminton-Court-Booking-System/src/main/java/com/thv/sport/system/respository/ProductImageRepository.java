package com.thv.sport.system.respository;

import com.thv.sport.system.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	@Modifying
	@Transactional
	@Query("delete from ProductImage pi where pi.product.productId = :productId")
	void deleteByProductId(@Param("productId") Long productId);

}

