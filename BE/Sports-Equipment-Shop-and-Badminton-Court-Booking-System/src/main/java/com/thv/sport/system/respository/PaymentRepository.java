package com.thv.sport.system.respository;

import com.thv.sport.system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);

    List<Payment> findByStatus(String status);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}

