package com.thv.sport.system.dto.response.booking;

import com.thv.sport.system.model.BookingItem;
import com.thv.sport.system.model.CourtCenter;
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

    private List<BookingItem> bookingItems;

    private CourtCenter courtCenter;

    private String courtCenterName;

    private String courtCenterAddress;

    private String courtCenterPhoneNumber;
}
