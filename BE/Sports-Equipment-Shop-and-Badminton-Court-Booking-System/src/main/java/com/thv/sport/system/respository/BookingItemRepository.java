package com.thv.sport.system.respository;

import com.thv.sport.system.model.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    @Query("""
                SELECT bi FROM BookingItem bi
                JOIN bi.booking b
                WHERE bi.court.courtId IN :courtIds
                AND bi.bookingDate = :date
                AND (
                    b.status = 'confirmed'
                    OR (b.status = 'pending' AND b.expiredAt > CURRENT_TIMESTAMP)
                )
                AND (
                    bi.startTime < :endTime
                    AND bi.endTime > :startTime
                )
            """)
    List<BookingItem> findConflicts(
            List<Long> courtIds,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
}
