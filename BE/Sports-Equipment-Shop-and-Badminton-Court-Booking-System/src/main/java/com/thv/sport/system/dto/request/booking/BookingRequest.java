package com.thv.sport.system.dto.request.booking;

import com.thv.sport.system.dto.request.court.CourtRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    private List<BookingItemRequest> items;
    private String recipient;
    private String phoneNumber;
}
