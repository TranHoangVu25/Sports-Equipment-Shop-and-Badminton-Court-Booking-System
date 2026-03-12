package com.thv.sport.system.respository;

import com.thv.sport.system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}

