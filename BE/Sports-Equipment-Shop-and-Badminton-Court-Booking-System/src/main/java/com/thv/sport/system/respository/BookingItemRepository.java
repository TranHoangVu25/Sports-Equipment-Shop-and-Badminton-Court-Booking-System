package com.thv.sport.system.respository;

import com.thv.sport.system.model.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    @Query("""
            SELECT b FROM BookingItem b
            WHERE b.court.courtId IN :courtIds
            AND b.bookingDate = :date
            AND (
                (b.startTime < :endTime AND b.endTime > :startTime)
            )
            """)
    List<BookingItem> findConflicts(
            List<Long> courtIds,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
}
