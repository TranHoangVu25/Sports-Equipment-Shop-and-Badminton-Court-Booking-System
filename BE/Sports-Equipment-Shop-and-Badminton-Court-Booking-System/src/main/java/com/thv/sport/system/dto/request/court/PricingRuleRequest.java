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

    //thứ nào trong tuần
    private Integer dayOfWeek;

    //ngày đặc biệt
    private LocalDate specificDate;

    private LocalTime startTime;

    private LocalTime endTime;

    //giá mỗi giờ
    private BigDecimal pricePerHour;

    private String ruleType;

    //độ ưu tiên của giá
    private Integer priority;

    //còn sân hay k
    private Boolean active;
}
