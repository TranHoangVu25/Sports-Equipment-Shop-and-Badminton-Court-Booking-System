package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.booking.BookingRequest;
import com.thv.sport.system.dto.response.booking.BookingResponse;
import com.thv.sport.system.model.Booking;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getBookingList(Long userId);

    String checkoutBooking(Long userId, BookingRequest request);

    BookingResponse getBookingDetail(Long bookingId, Long userId);
}
