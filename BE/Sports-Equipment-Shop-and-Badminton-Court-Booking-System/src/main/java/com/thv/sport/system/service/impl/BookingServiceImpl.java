package com.thv.sport.system.service.impl;

import com.thv.sport.system.dto.request.booking.BookingItemRequest;
import com.thv.sport.system.dto.request.booking.BookingRequest;
import com.thv.sport.system.dto.response.booking.BookingResponse;
import com.thv.sport.system.model.Booking;
import com.thv.sport.system.model.BookingItem;
import com.thv.sport.system.model.BookingPayment;
import com.thv.sport.system.model.Court;
import com.thv.sport.system.model.PricingRule;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.BookingItemRepository;
import com.thv.sport.system.respository.BookingPaymentRepository;
import com.thv.sport.system.respository.BookingRepository;
import com.thv.sport.system.respository.CourtCenterRepository;
import com.thv.sport.system.respository.CourtRepository;
import com.thv.sport.system.respository.PricingRuleRepository;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.BookingService;
import com.thv.sport.system.service.MomoCheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final CourtCenterRepository courtCenterRepository;
    private final CourtRepository courtRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final MomoCheckoutService momoCheckoutService;

    @Override
    public List<BookingResponse> getBookingList() {
        return List.of();
    }

    @Transactional
    @Override
    public String checkoutBooking(Long userId, BookingRequest request) {
        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDateTime now = LocalDateTime.now();

            // =========================
            // 1. PRELOAD COURT
            // =========================
            List<Long> courtIds = request.getItems().stream()
                    .map(BookingItemRequest::getCourtId)
                    .distinct()
                    .toList();

            List<Court> courts = courtRepository.findCourtByCourtId(courtIds);

            if (courts.size() != courtIds.size()) {
                throw new RuntimeException("Some courts not found");
            }

            Map<Long, Court> courtMap = courts.stream()
                    .collect(Collectors.toMap(Court::getCourtId, c -> c));

            // =========================
            // 2. PRELOAD PRICING
            // =========================
            List<Long> centerIds = courts.stream()
                    .map(c -> c.getCourtCenter().getCourtCenterId())
                    .distinct()
                    .toList();

            List<PricingRule> rules = pricingRuleRepository.findByCenterIds(centerIds);

            Map<Long, List<PricingRule>> pricingMap = rules.stream()
                    .filter(PricingRule::getActive)
                    .collect(Collectors.groupingBy(
                            r -> r.getCourtCenter().getCourtCenterId()
                    ));

            // =========================
            // 3. BATCH CHECK CONFLICT
            // =========================
            // (giả sử tất cả request cùng 1 ngày, nếu không thì group theo date)
            LocalDate bookingDate = request.getItems().get(0).getBookingDate();

            LocalTime minStart = request.getItems().stream()
                    .map(BookingItemRequest::getStartTime)
                    .min(LocalTime::compareTo)
                    .get();

            LocalTime maxEnd = request.getItems().stream()
                    .map(BookingItemRequest::getEndTime)
                    .max(LocalTime::compareTo)
                    .get();

            List<BookingItem> conflicts = bookingItemRepository.findConflicts(
                    courtIds,
                    bookingDate,
                    minStart,
                    maxEnd
            );

            Map<Long, List<BookingItem>> conflictMap = conflicts.stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getCourt().getCourtId()
                    ));

            // =========================
            // 4. CREATE BOOKING
            // =========================
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setStatus("PENDING");
            booking.setRecipient(request.getRecipient());
            booking.setPhoneNumber(request.getPhoneNumber());
            booking.setBookingDate(bookingDate);
            booking.setUpdatedAt(now);

            List<BookingItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            // =========================
            // 5. LOOP (NO QUERY INSIDE)
            // =========================
            for (BookingItemRequest req : request.getItems()) {

                Court court = courtMap.get(req.getCourtId());

                // ===== check conflict in-memory =====
                List<BookingItem> courtConflicts = conflictMap.get(req.getCourtId());

                if (courtConflicts != null) {
                    for (BookingItem booked : courtConflicts) {
                        if (!(req.getEndTime().isBefore(booked.getStartTime()) ||
                                req.getStartTime().isAfter(booked.getEndTime()))) {

                            throw new RuntimeException("Slot already booked");
                        }
                    }
                }

                // ===== pricing =====
                Long centerId = court.getCourtCenter().getCourtCenterId();
                List<PricingRule> centerRules = pricingMap.get(centerId);

                if (centerRules == null || centerRules.isEmpty()) {
                    throw new RuntimeException("No pricing rule");
                }

                BigDecimal itemTotal = calculatePrice(
                        centerRules,
                        req.getBookingDate(),
                        req.getStartTime(),
                        req.getEndTime()
                );

                BookingItem item = BookingItem.builder()
                        .booking(booking)
                        .court(court)
                        .bookingDate(req.getBookingDate())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .totalPrice(itemTotal)
                        .build();

                totalAmount = totalAmount.add(itemTotal);
                items.add(item);
            }

            booking.setDetails(items);
            booking.setTotalAmount(totalAmount);

            Booking savedBooking = bookingRepository.save(booking);

            // =========================
            // 6. PAYMENT
            // =========================
            BookingPayment payment = BookingPayment.builder()
                    .booking(savedBooking)
                    .amount(totalAmount)
                    .currency("VND")
                    .paymentMethod("MOMO")
                    .status("PENDING")
                    .createdAt(now)
                    .build();

            bookingPaymentRepository.save(payment);

            // =========================
            // 7. CALL MOMO
            // =========================
            return momoCheckoutService.createPayment(savedBooking, payment, userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookingResponse getBookingDetail(Long bookingId) {
        return null;
    }

    public BigDecimal calculatePrice(
            List<PricingRule> rules,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {

        BigDecimal total = BigDecimal.ZERO;

        LocalTime cursor = start;

        while (cursor.isBefore(end)) {

            LocalTime next = cursor.plusHours(1);

            LocalTime finalCursor = cursor;
            PricingRule matched = rules.stream()
                    .filter(r -> {

                        if (r.getSpecificDate() != null) {
                            if (!r.getSpecificDate().equals(date)) return false;
                        } else {
                            if (!r.getDayOfWeek().equals(date.getDayOfWeek().getValue())) return false;
                        }

                        return !finalCursor.isBefore(r.getStartTime()) &&
                                finalCursor.isBefore(r.getEndTime());
                    }).max(Comparator.comparing(PricingRule::getPriority))
                    .orElseThrow(() -> new RuntimeException("No pricing rule"));

            total = total.add(matched.getPricePerHour());

            cursor = next;
        }

        return total;
    }
}
