package com.thv.sport.system.dto.response.courtcenter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtCenterResponse {
    private Long courtCenterId;
    private String name;
    private String locationDetail;
    private String phoneNumber;
    private Integer deleted;
    private String imgUrl;
    private LocalDateTime createdAt;
    private Double latitude;
    private Double longitude;
    private String mapUrl;
    private Double distance;
}
