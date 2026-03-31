package com.thv.sport.system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_center_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtCenterImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_center_id", nullable = false)
    @JsonBackReference(value = "center-image")
    private CourtCenter courtCenter;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail; // ảnh đại diện

    @Column(name = "sort_order")
    private Integer sortOrder; // thứ tự hiển thị

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}