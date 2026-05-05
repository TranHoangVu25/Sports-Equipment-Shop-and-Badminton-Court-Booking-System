package com.thv.sport.system.respository;

import com.thv.sport.system.model.CourtCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtCenterRepository extends JpaRepository<CourtCenter, Long> {
    @Query("""
                SELECT c FROM CourtCenter c
                WHERE (
                    :name IS NULL 
                    OR :name = '' 
                    OR LOWER(c.name) LIKE LOWER('%' || :name || '%')
                )
                AND c.deleted = :deleted
                ORDER BY c.createdAt DESC
            """)
    Page<CourtCenter> searchCourtCenter(
            @Param("name") String name,
            @Param("deleted") Integer deleted,
            Pageable pageable
    );
}

