package com.thv.sport.system.dto.request.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
