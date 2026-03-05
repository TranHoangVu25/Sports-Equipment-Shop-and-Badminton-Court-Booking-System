package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_center_id")
    private Long courtCenterId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "location_detail", length = 255)
    private String locationDetail;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}

