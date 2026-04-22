package com.thv.sport.system.respository;

import com.thv.sport.system.model.BookingPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingPaymentRepository extends JpaRepository<BookingPayment, Long> {
}
