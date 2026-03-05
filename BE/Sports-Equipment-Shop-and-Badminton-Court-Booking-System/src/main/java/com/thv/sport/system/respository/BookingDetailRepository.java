package com.thv.sport.system.respository;

import com.thv.sport.system.model.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    Optional<BookingDetail> findByBookingId(Long bookingId);
}

