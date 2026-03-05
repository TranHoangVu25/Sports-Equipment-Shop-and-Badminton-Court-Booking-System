package com.thv.sport.system.respository;

import com.thv.sport.system.model.BookingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingPaymentRepository extends JpaRepository<BookingPayment, Long> {
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.bookingId = :bookingId")
    List<BookingPayment> findByBookingId(@Param("bookingId") Long bookingId);

    List<BookingPayment> findByStatus(String status);
}


