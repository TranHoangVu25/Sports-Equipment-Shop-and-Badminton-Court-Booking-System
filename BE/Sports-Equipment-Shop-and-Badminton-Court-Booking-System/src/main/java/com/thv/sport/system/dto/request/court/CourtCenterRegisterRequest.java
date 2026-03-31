package com.thv.sport.system.dto.request.court;

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
public class CourtCenterRegisterRequest {

    private Long courtCenterId; // dùng cho update

    private String name;
    private String locationDetail;
    private String phoneNumber;
    private String imageUrl;

    //danh sách sân
    private List<CourtRequest> courts;

    //giờ hoạt động
    private List<CourtSlotRequest> slots;

    //bảng giá
    private List<PricingRuleRequest> pricingRules;

    private List<CourtCenterImageRequest> images;
}
