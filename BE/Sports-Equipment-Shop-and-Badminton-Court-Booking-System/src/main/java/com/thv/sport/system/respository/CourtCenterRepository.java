package com.thv.sport.system.respository;

import com.thv.sport.system.model.CourtCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourtCenterRepository extends JpaRepository<CourtCenter, Long> {
    Optional<CourtCenter> findByName(String name);
}

