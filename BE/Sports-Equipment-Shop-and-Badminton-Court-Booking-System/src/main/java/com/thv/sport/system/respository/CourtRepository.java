package com.thv.sport.system.respository;

import com.thv.sport.system.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    @Query("SELECT c FROM Court c WHERE c.courtCenter.courtCenterId = :courtCenterId")
    List<Court> findByCourtCenterId(@Param("courtCenterId") Long courtCenterId);

    List<Court> findByStatus(Integer status);

    @Query("SELECT c FROM Court c WHERE c.courtCenter.courtCenterId = :courtCenterId AND c.status = :status")
    List<Court> findByCourtCenterIdAndStatus(@Param("courtCenterId") Long courtCenterId, @Param("status") Integer status);
}


