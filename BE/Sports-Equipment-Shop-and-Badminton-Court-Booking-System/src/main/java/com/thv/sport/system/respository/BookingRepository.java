package com.thv.sport.system.respository;

import com.thv.sport.system.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

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
            ORDER BY b.bookingId DESC
            """)
    Page<Booking> findAllBooking(
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
            WHERE b.bookingId =:bookingId
            ORDER BY b.bookingDate DESC
            """)
    Booking findBookingDetailByBookingId(
            Long bookingId
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

    // Revenue grouped by day for a given year + month (only bookings with given status)
    @Query("""
            SELECT DAY(b.createdAt),
                   COALESCE(SUM(b.totalAmount),0)
            FROM Booking b
            WHERE YEAR(b.createdAt) = :year
              AND MONTH(b.createdAt) = :month
              AND b.status = :status
            GROUP BY DAY(b.createdAt)
            ORDER BY DAY(b.createdAt)
    """)
    List<Object[]> revenueByMonth(
            Integer year,
            Integer month,
            String status
    );

    // Revenue grouped by month for a given year (only bookings with given status)
    @Query("""
            SELECT MONTH(b.createdAt),
                   COALESCE(SUM(b.totalAmount),0)
            FROM Booking b
            WHERE YEAR(b.createdAt) = :year
              AND b.status = :status
            GROUP BY MONTH(b.createdAt)
            ORDER BY MONTH(b.createdAt)
    """)
    List<Object[]> revenueByYear(
            Integer year,
            String status
    );
}
