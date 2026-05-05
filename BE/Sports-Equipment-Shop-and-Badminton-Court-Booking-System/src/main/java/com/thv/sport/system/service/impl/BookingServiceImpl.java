package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

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
    public List<BookingResponse> getBookingList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user.not.found"));

        List<BookingResponse> responseList = new ArrayList<>();

        List<Booking> bookingList = bookingRepository.findAllBookingByUserId(user.getUserId());

        for (Booking booking : bookingList) {
            BookingResponse response = BookingResponse.builder()
                    .bookingId(booking.getBookingId())
                    .userId(user.getUserId())
                    .userName(user.getFullName())
                    .bookingDate(booking.getBookingDate())
                    .totalAmount(booking.getTotalAmount())
                    .status(booking.getStatus())
                    .bookingItems(booking.getBookingItems())
//                    .courtCenter(booking.getBookingItems().getFirst().getCourt().getCourtCenter())
                    .courtCenterName(booking.getBookingItems().getFirst().getCourt().getCourtCenter().getName())
                    .courtCenterAddress(booking.getBookingItems().getFirst().getCourt().getCourtCenter()
                            .getLocationDetail())
                    .courtCenterPhoneNumber(booking.getBookingItems().getFirst().getCourt().getCourtCenter()
                            .getPhoneNumber())
                    .build();

            responseList.add(response);
        }
        return responseList;
    }

    @Transactional
    @Override
    public String checkoutBooking(Long userId, BookingRequest request) {
        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("user.not.found"));

            LocalDateTime now = LocalDateTime.now();

            // =========================
            // 0. CHECK EXISTING BOOKING (🔥 NEW)
            // =========================
            List<Booking> list = bookingRepository.findPendingBookings(
                    userId,
                    Constants.PaymentStatus.PENDING,
                    now
            );
            Booking booking = list.isEmpty() ? null : list.getFirst();
            boolean isNewBooking = (booking == null);

            // =========================
            // 1. PRELOAD COURT (chỉ khi tạo mới)
            // =========================
            List<Long> courtIds = request.getItems().stream()
                    .map(BookingItemRequest::getCourtId)
                    .distinct()
                    .toList();

            List<Court> courts = courtRepository.findCourtByCourtId(courtIds);

            if (courts.size() != courtIds.size()) {
                throw new RuntimeException("court.not.found");
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
            LocalDate bookingDate = request.getItems().getFirst().getBookingDate();

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
            // 4. CREATE / UPDATE BOOKING
            // =========================
            if (isNewBooking) {
                booking = new Booking();
            booking.setUser(user);
                booking.setStatus(Constants.BookingStatus.PENDING);
                booking.setExpiredAt(now.plusMinutes(Constants.TtlTIme.TIME));
                booking.setCreatedAt(now);
            } else {
                // 🔥 refresh TTL
                booking.setExpiredAt(now.plusMinutes(Constants.TtlTIme.TIME));
                booking.getBookingItems().clear(); // reset items
            }

            booking.setRecipient(request.getRecipient());
            booking.setPhoneNumber(request.getPhoneNumber());
            booking.setBookingDate(bookingDate);
            booking.setUpdatedAt(now);

            List<BookingItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            // =========================
            // 5. LOOP
            // =========================
            for (BookingItemRequest req : request.getItems()) {

                Court court = courtMap.get(req.getCourtId());

                // ===== conflict check =====
                List<BookingItem> courtConflicts = conflictMap.get(req.getCourtId());

                if (courtConflicts != null) {
                    for (BookingItem booked : courtConflicts) {

                        //bỏ qua chính booking của mình
                        if (!isNewBooking && booked.getBooking().getBookingId()
                                .equals(booking.getBookingId())) {
                            continue;
                        }

                        if (!(req.getEndTime().isBefore(booked.getStartTime()) ||
                                req.getStartTime().isAfter(booked.getEndTime()))) {

                            throw new RuntimeException("court.was.booked");
                        }
                    }
                }

                // ===== pricing =====
                Long centerId = court.getCourtCenter().getCourtCenterId();
                List<PricingRule> centerRules = pricingMap.get(centerId);

                if (centerRules == null || centerRules.isEmpty()) {
                    throw new RuntimeException("pricing.rule.not.existed");
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

            booking.setBookingItems(items);
            booking.setTotalAmount(totalAmount);

            Booking savedBooking = bookingRepository.save(booking);

            // =========================
            // 6. ALWAYS CREATE NEW PAYMENT
            // =========================
            BookingPayment payment = BookingPayment.builder()
                    .booking(savedBooking)
                    .amount(totalAmount)
                    .currency(Constants.Currency.VND)
                    .paymentMethod(Constants.CheckoutMethod.MOMO)
                    .status(Constants.PaymentStatus.PENDING)
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
    public BookingResponse getBookingDetail(Long bookingId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user.not.found"));

        Booking booking = bookingRepository.findBookingDetailByUserIdAndBookingId(user.getUserId(), bookingId);

        if (ObjectUtils.isEmpty(booking)) {
            throw new RuntimeException("booking.not.found");
        }

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .bookingDate(booking.getBookingDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .bookingItems(booking.getBookingItems())
//                    .courtCenter(booking.getBookingItems().getFirst().getCourt().getCourtCenter())
                .courtCenterName(booking.getBookingItems().getFirst().getCourt().getCourtCenter().getName())
                .courtCenterAddress(booking.getBookingItems().getFirst().getCourt().getCourtCenter()
                        .getLocationDetail())
                .courtCenterPhoneNumber(booking.getBookingItems().getFirst().getCourt().getCourtCenter()
                        .getPhoneNumber())
                .build();
    }

    public BigDecimal calculatePrice(
            List<PricingRule> rules,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {
        try {
            // Validate input time range
            if (start == null || end == null || !start.isBefore(end)) {
                throw new RuntimeException("invalid.time.range");
            }

            BigDecimal total = BigDecimal.ZERO;
            LocalTime cursor = start;

            // Iterate through the time range in 30-minute steps
            while (cursor.isBefore(end)) {

                // Determine next time slot (30 minutes)
                LocalTime next = cursor.plusMinutes(30);
                if (next.isAfter(end)) {
                    next = end;
                }

                LocalTime current = cursor;

                // Find the applicable pricing rule for the current time slot
                PricingRule matched = rules.stream()
                        .filter(r -> {

                            // If a specific date rule exists, it has higher priority
                            if (r.getSpecificDate() != null) {
                                if (!r.getSpecificDate().equals(date)) return false;
                            } else {
                                // Otherwise, match by day of week
                                if (!r.getDayOfWeek().equals(date.getDayOfWeek().getValue())) return false;
                            }

                            // Check if current time falls within rule time range
                            return !current.isBefore(r.getStartTime()) &&
                                    current.isBefore(r.getEndTime());
                        })
                        // Select the rule with the highest priority
                        .max(Comparator.comparing(PricingRule::getPriority))
                        .orElseThrow(() -> new RuntimeException("No pricing rule"));

                // Calculate duration of this slot in minutes
                long minutes = java.time.Duration.between(cursor, next).toMinutes();

                // Convert hourly price to per-minute price
                BigDecimal pricePerMinute = matched.getPricePerHour()
                        .divide(BigDecimal.valueOf(60), 4, java.math.RoundingMode.HALF_UP);

                // Calculate price for this slot
                BigDecimal slotPrice = pricePerMinute.multiply(BigDecimal.valueOf(minutes));

                // Add to total
                total = total.add(slotPrice);

                // Move to next slot
                cursor = next;
            }

            // Round final result to integer (VND currency)
            return total.setScale(0, java.math.RoundingMode.HALF_UP);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredBooking() {

        LocalDateTime now = LocalDateTime.now();

        bookingRepository.cancelExpiredBookings(
                Constants.PaymentStatus.PENDING,
                now
        );
    }
}
