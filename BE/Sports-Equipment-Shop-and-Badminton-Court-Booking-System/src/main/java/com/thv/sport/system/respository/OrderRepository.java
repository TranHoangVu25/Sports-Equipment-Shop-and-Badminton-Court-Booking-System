package com.thv.sport.system.respository;

import com.thv.sport.system.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // find all order in system
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    //find order by user id
    Page<Order> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.user.userId = :userId")
    Optional<Order> findOrderByOrderIdAndUserId(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    @Query("SELECT count(o) FROM Order o  WHERE o.user.userId = :userId")
    int getNumberOfOrdersByUserId(
            @Param("userId") Long userId
    );
}

