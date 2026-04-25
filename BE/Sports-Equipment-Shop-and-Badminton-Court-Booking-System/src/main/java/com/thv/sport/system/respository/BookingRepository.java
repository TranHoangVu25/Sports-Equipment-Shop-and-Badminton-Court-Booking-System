package com.thv.sport.system.respository;

import com.thv.sport.system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.userId =:userId
            ORDER BY b.bookingDate DESC
            """)
    List<Booking> findAllBookingByUserId(
            Long userId
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.userId =:userId
                        and b.bookingId =:bookingId
            ORDER BY b.bookingDate DESC
            """)
    Booking findBookingDetailByUserIdAndBookingId(
            Long userId, Long bookingId
    );
}
