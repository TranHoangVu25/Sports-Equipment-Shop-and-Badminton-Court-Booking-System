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
}
