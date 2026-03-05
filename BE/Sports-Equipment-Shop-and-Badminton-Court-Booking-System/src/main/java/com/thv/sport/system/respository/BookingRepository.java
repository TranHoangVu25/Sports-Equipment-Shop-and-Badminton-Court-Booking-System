package com.thv.sport.system.respository;

import com.thv.sport.system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.court.courtId = :courtId")
    List<Booking> findByCourtId(@Param("courtId") Long courtId);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId AND b.court.courtId = :courtId")
    List<Booking> findByUserIdAndCourtId(@Param("userId") Long userId, @Param("courtId") Long courtId);
}


