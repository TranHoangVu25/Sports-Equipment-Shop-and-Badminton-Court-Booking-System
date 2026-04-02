package com.thv.sport.system.dto.request.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtSlotRequest {

    private Long id;

    private Integer dayOfWeek; // 2-8

    private LocalTime startTime;
    private LocalTime endTime;

    private String status;
}