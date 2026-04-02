package com.thv.sport.system.respository;

import com.thv.sport.system.model.CourtCenterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtCenterImageRepository extends JpaRepository<CourtCenterImage, Long> {

}


