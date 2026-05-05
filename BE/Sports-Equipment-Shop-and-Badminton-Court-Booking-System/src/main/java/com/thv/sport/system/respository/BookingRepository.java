package com.thv.sport.system.respository;

import com.thv.sport.system.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.userId =:userId
            ORDER BY b.bookingId DESC
            """)
    Page<Booking> findAllBookingByUserId(
            Long userId,
            Pageable pageable
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

    @Query("""
                SELECT b FROM Booking b
                WHERE b.user.userId = :userId
                AND b.status = :status
                AND b.expiredAt > :now
                ORDER BY b.createdAt DESC
            """)
    List<Booking> findPendingBookings(
            Long userId,
            String status,
            LocalDateTime now
    );

    @Modifying
    @Query("""
                UPDATE Booking b
                SET b.status = 'CANCELLED',
                    b.updatedAt = :now
                WHERE b.status = :status
                AND b.expiredAt <= :now
            """)
    int cancelExpiredBookings(String status, LocalDateTime now);
}
