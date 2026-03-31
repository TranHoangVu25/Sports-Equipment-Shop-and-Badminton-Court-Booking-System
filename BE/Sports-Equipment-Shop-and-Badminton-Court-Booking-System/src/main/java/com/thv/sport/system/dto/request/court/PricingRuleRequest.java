package com.thv.sport.system.dto.request.court;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleRequest {

    private Long id;

    private Integer dayOfWeek;

    private LocalDate specificDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal pricePerHour;

    private String ruleType;

    private Integer priority;

    private Boolean active;
}
