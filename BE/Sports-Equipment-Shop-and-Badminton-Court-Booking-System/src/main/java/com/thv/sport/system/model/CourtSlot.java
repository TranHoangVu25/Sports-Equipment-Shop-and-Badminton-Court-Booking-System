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

import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "court_slot")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourtSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 gắn với center (KHÔNG gắn court)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_center_id", nullable = false)
    @JsonBackReference(value = "center-slot")
    private CourtCenter courtCenter;

    // 2: Monday ... 8: Sunday
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "status")
    private Integer status; // mở / đóng

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}