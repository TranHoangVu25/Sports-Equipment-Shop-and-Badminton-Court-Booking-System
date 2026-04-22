package com.thv.sport.system.dto.request.booking;

import com.thv.sport.system.model.Booking;
import com.thv.sport.system.model.BookingPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCourtRequest {
    private Booking booking;
    private BookingPayment payment;
}
