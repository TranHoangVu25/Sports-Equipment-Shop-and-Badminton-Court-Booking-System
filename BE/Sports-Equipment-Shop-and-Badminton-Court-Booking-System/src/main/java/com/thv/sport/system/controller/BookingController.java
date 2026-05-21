package com.thv.sport.system.controller;


import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.security.UserPrincipal;
import com.thv.sport.system.dto.response.BaseResponse;
import com.thv.sport.system.dto.response.booking.BookingResponse;
import com.thv.sport.system.model.Booking;
import com.thv.sport.system.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
@RestController
@RequestMapping(Constants.ApiPath.API_BOOKING)
@Slf4j
public class BookingController extends BaseController {
    private final BookingService bookingService;
    @GetMapping("/get-user-booking-list")
    @Operation(
            summary = "get all user booking",
            description = "get all user booking by userId",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Update template successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user is not authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<BaseResponse<Page<BookingResponse>>> getHomeProducts(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Long userId = Long.valueOf(user.getUserId());

        Page<BookingResponse> response =
                bookingService.getBookingList(userId, page, size);

        return successResponse(response, "common.success", null);
    }

    @GetMapping("/get-booking-detail/{bookingId}")
    public ResponseEntity<BaseResponse<BookingResponse>> getHomeProducts(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long bookingId
    ) {
        Long userId = Long.valueOf(user.getUserId());
        BookingResponse response = bookingService.getBookingDetail(bookingId, userId);
        return successResponse(response, "common.success", null);
    }

    /**
     * Get occupied time ranges for given courts on a date within a time window
     * status = 'confirmed' OR (status = 'pending' AND expiredAt > now)
     */
    @GetMapping("/occupied")
    public ResponseEntity<BaseResponse<List<com.thv.sport.system.dto.response.booking.BookingItemResponse>>> getOccupied(
            @RequestParam List<Long> courtIds,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        try {
            LocalDate d = LocalDate.parse(date);
            LocalTime s = LocalTime.parse(startTime);
            LocalTime e = LocalTime.parse(endTime);

            List<com.thv.sport.system.dto.response.booking.BookingItemResponse> resp = bookingService.getBookedSlots(
                    courtIds, d, s, e
            );

            return successResponse(resp, "common.success", null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
