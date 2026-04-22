package com.thv.sport.system.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingItemResponse {

    private Long id;

    private Long courtId;
    private String courtName; // nếu cần hiển thị UI

    private LocalDate bookingDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal pricePerHour;

    private BigDecimal totalPrice;
}
