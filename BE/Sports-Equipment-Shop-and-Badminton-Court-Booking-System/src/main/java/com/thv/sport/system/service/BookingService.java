package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.booking.BookingRequest;
import com.thv.sport.system.dto.response.booking.BookingResponse;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getBookingList();

    String checkoutBooking(Long userId, BookingRequest request);

    BookingResponse getBookingDetail(Long bookingId);
}
