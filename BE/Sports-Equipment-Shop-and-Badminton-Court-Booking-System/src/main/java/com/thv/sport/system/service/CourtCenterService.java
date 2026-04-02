package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.dto.response.courtcenter.CourtCenterResponse;
import com.thv.sport.system.model.CourtCenter;

import java.util.List;

public interface CourtCenterService {
    CourtCenter registerCourt(CourtCenterRegisterRequest request);

    CourtCenter updateCourt(CourtCenterRegisterRequest request, Long courtCenterId);

    CourtCenter getCourtDetails(Long courtCenterId);

    List<CourtCenterResponse> search();

    void deleteCourtCenter(List<Long> ids);
}
