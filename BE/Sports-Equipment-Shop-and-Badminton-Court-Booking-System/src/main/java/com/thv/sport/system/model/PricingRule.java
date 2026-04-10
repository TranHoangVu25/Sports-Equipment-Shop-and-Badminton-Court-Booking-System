package com.thv.sport.system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "pricing_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //gắn với center
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_center_id", nullable = false)
    @JsonBackReference(value = "center-pricing")
    private CourtCenter courtCenter;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    //Dùng khi bạn muốn áp dụng giá cho một ngày cụ thể
    // ưu tiên cao hơn dayOfWeek
    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    //NORMAL → giá bình thường
    //PEAK → giờ cao điểm (18h–22h)
    //HOLIDAY → ngày lễ
    @Column(name = "rule_type")
    private String ruleType; // NORMAL / PEAK / HOLIDAY

    //ví dụ: A : Thứ 7 -> priority 1
    // B: Thứ 7 + giờ 18h-22h -> priority 2
    //rule có priority cao hơn thì được chọn
    @Column(name = "priority")
    private Integer priority; // ưu tiên rule

    //bật / tắt rule mà không cần xóa DB
    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt;
}
