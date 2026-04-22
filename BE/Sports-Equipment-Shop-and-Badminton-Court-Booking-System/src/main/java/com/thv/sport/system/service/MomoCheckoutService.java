package com.thv.sport.system.service;

import com.thv.sport.system.model.Booking;
import com.thv.sport.system.model.BookingPayment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface MomoCheckoutService {
    String createPayment(Booking booking, BookingPayment payment, Long userId);
    ResponseEntity<String> handleMomoIpn(HttpServletRequest request);
}
