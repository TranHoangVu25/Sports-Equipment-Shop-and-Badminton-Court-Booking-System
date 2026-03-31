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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "pricing_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 gắn với center
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_center_id", nullable = false)
    @JsonBackReference(value = "center-pricing")
    private CourtCenter courtCenter;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "specific_date")
    private LocalDate specificDate; // ưu tiên cao hơn dayOfWeek

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "rule_type")
    private String ruleType; // NORMAL / PEAK / HOLIDAY

    @Column(name = "priority")
    private Integer priority; // ưu tiên rule

    @Column(name = "active")
    private Boolean active;
}