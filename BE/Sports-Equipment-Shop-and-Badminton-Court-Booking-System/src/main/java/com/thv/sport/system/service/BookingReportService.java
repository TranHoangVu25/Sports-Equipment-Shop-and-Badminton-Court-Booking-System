package com.thv.sport.system.service;

import com.thv.sport.system.dto.response.report.RevenueChartResponse;

import java.util.List;

public interface BookingReportService {

    /**
     * type: 0 = month (require month param), 1 = year
     */
    List<RevenueChartResponse> getRevenue(Integer type, Integer year, Integer month);

}

