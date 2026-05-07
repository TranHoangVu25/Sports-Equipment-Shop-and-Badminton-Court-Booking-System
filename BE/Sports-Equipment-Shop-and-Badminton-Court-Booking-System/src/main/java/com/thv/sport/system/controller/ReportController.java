package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.report.RevenueChartResponse;
import com.thv.sport.system.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.ApiPath.API_REPORT)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final com.thv.sport.system.service.BookingReportService bookingReportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<RevenueChartResponse>>> getRevenue(
            @RequestParam Integer type,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<RevenueChartResponse> data = reportService.getRevenue(type, year, month);
        return ResponseEntity.ok(
                ApiResponse.<List<RevenueChartResponse>>builder()
                        .message("Get revenue statistics successfully")
                        .result(data)
                        .build()
        );
    }

    @GetMapping("/revenue/booking")
    public ResponseEntity<ApiResponse<List<RevenueChartResponse>>> getBookingRevenue(
            @RequestParam Integer type,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<RevenueChartResponse> data = bookingReportService.getRevenue(type, year, month);
        return ResponseEntity.ok(
                ApiResponse.<List<RevenueChartResponse>>builder()
                        .message("Get booking revenue statistics successfully")
                        .result(data)
                        .build()
        );
    }
}

