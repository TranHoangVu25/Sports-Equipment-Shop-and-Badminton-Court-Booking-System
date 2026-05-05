package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.dto.response.courtcenter.CourtCenterResponse;
import com.thv.sport.system.model.CourtCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourtCenterService {
    CourtCenter registerCourt(CourtCenterRegisterRequest request);

    CourtCenter updateCourt(CourtCenterRegisterRequest request, Long courtCenterId);

    CourtCenter getCourtDetails(Long courtCenterId);

    Page<CourtCenterResponse> search(String name, Double userLat, Double userLng, Pageable pageable);

    void deleteCourtCenter(List<Long> ids);
}
