package com.thv.sport.system.dto.request.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {
    private String city;
    private String locationDetail;
    private String phoneNumber;
    private String recipientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDefaultAddress = false;
    private String country;
    private String province;
}
