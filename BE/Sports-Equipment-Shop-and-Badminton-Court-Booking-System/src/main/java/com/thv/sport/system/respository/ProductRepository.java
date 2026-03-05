package com.thv.sport.system.respository;

import com.thv.sport.system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMainCategory(String mainCategory);
    List<Product> findBySubCategory(String subCategory);
    List<Product> findByStatus(String status);
    Optional<Product> findByName(String name);
}

