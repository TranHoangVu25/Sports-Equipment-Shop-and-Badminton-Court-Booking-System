package com.thv.sport.system.dto.request.court;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtCenterImageRequest {

    private Long imageId; // dùng cho update

    private String imageUrl;

    private Boolean isThumbnail;

    private Integer sortOrder;
}