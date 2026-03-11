package com.thv.sport.system.respository;

import com.thv.sport.system.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    boolean existsByUserUserId(Integer userId);

}

