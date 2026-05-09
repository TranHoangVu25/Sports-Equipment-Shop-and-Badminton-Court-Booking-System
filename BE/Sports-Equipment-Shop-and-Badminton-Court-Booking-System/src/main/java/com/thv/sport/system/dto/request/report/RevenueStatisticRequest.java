package com.thv.sport.system.dto.request.report;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for revenue statistic query
 */
@Getter
@Setter
public class RevenueStatisticRequest {

    /**
     * 0 = month
     * 1 = year
     */
    private Integer type;

    private Integer year;

    private Integer month;
}

