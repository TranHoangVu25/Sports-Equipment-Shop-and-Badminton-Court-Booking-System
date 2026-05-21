package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.booking.BookingRequest;
import com.thv.sport.system.dto.response.booking.BookingResponse;
import org.springframework.data.domain.Page;

public interface BookingService {
    Page<BookingResponse> getBookingList(Long userId, int page, int size);

    String checkoutBooking(Long userId, BookingRequest request);

    BookingResponse getBookingDetail(Long bookingId, Long userId);

    java.util.List<com.thv.sport.system.dto.response.booking.BookingItemResponse> getBookedSlots(
            java.util.List<Long> courtIds,
            java.time.LocalDate date,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime
    );
}
