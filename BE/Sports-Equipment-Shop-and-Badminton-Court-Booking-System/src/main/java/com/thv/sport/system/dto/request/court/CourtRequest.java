package com.thv.sport.system.dto.request.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtRequest {

    private Long courtId; // null nếu create

    private String name;
    private String type;
    private String status;
}
