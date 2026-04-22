package com.thv.sport.system.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;

    private Long userId;
    private String userName; // optional

    private LocalDate bookingDate;

    private BigDecimal totalAmount;

    private String status;

    private LocalDateTime createdAt;

    private List<BookingItemResponse> details;

    private List<BookingPaymentResponse> payments;
}
