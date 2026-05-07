package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.response.report.RevenueChartResponse;
import com.thv.sport.system.respository.BookingRepository;
import com.thv.sport.system.service.BookingReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReportServiceImpl implements BookingReportService {

    private final BookingRepository bookingRepository;

    @Override
    public List<RevenueChartResponse> getRevenue(Integer type, Integer year, Integer month) {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");

        if (type == null || year == null) {
            throw new IllegalArgumentException("type and year are required");
        }

        if (type == 0) {
            if (month == null) {
                throw new IllegalArgumentException("month is required when type=0");
            }

            List<Object[]> result = bookingRepository.revenueByMonth(
                    year,
                    month,
                    Constants.BookingStatus.CONFIRMED
            );

            Map<Integer, BigDecimal> map = new HashMap<>();
            for (Object[] row : result) {
                Integer day = ((Number) row[0]).intValue();
                BigDecimal rev = (BigDecimal) row[1];
                map.put(day, rev == null ? BigDecimal.ZERO : rev);
            }

            YearMonth ym = YearMonth.of(year, month);
            int days = ym.lengthOfMonth();

            return IntStream.rangeClosed(1, days)
                    .mapToObj(d -> {
                        BigDecimal rev = map.getOrDefault(d, BigDecimal.ZERO);
                        double value = rev.doubleValue();
                        String resultDecimal = df.format(value);
                        String label = LocalDate.of(year, month, d).toString();
                        String tooltip = String.format("%,.0f VND", value);
                        return new RevenueChartResponse(label, resultDecimal, tooltip);
                    })
                    .collect(Collectors.toList());

        } else if (type == 1) {
            List<Object[]> result = bookingRepository.revenueByYear(
                    year,
                    Constants.BookingStatus.CONFIRMED
            );

            Map<Integer, BigDecimal> map = new HashMap<>();
            for (Object[] row : result) {
                Integer m = ((Number) row[0]).intValue();
                BigDecimal rev = (BigDecimal) row[1];
                map.put(m, rev == null ? BigDecimal.ZERO : rev);
            }

            return IntStream.rangeClosed(1, 12)
                    .mapToObj(m -> {
                        BigDecimal rev = map.getOrDefault(m, BigDecimal.ZERO);
                        double value = rev.doubleValue();
                        String resultDecimal = df.format(value);
                        String label = "Tháng " + m;
                        String tooltip = String.format("%,.0f VND", value);
                        return new RevenueChartResponse(label, resultDecimal, tooltip);
                    })
                    .collect(Collectors.toList());
        }

        throw new IllegalArgumentException("Invalid type value: " + type);
    }

}



