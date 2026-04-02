package com.thv.sport.system.respository;

import com.thv.sport.system.model.Court;
import com.thv.sport.system.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    @Query("select pr from PricingRule pr where pr.id in :ids")
    List<Court> findPricingRuleById(@Param("ids") List<Long> ids);
}


