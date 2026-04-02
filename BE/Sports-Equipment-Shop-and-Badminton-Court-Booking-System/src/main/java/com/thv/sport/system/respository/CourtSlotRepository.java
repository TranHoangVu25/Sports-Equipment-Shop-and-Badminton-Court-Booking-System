package com.thv.sport.system.respository;

import com.thv.sport.system.model.CourtSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtSlotRepository extends JpaRepository<CourtSlot, Long> {
    @Query("select cs from CourtSlot cs where cs.id in :ids")
    List<CourtSlot> findCourtSlotByCourtSlotId(@Param("ids") List<Long> ids);
}


