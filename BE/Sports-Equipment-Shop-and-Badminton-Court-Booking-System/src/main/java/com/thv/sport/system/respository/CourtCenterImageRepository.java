package com.thv.sport.system.respository;

import com.thv.sport.system.model.CourtCenterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtCenterImageRepository extends JpaRepository<CourtCenterImage, Long> {
    @Query("select cci from CourtCenterImage cci where cci.courtCenter.courtCenterId in :ids")
    List<CourtCenterImage> findListImgByCourCenterId(@Param("ids") List<Long> id);
}


