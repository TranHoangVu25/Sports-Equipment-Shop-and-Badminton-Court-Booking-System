package com.thv.sport.system.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "court_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
public class CourtCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_center_id")
    private Long courtCenterId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location_detail")
    private String locationDetail;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //0: không xóa, 1 xóa
    @Column(name = "deleted")
    private Integer deleted;

    //danh sách sân
    @OneToMany(mappedBy = "courtCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "center-court")
    private List<Court> courts;

    //giờ hoạt động chung
    @OneToMany(mappedBy = "courtCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "center-slot")
    private List<CourtSlot> slots;

    //rule giá chung
    @OneToMany(mappedBy = "courtCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "center-pricing")
    private List<PricingRule> pricingRules;

    //danh sách img
    @OneToMany(mappedBy = "courtCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "center-image")
    private List<CourtCenterImage> images;
}

