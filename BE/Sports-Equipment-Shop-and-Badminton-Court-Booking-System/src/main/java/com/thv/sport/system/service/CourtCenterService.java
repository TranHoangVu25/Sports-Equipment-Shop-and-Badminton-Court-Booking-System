package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.model.CourtCenter;

public interface CourtCenterService {
    CourtCenter registerCourt(CourtCenterRegisterRequest request);

    CourtCenter updateCourt(CourtCenterRegisterRequest request, Long courtCenterId);

    CourtCenter getCourtDetails(Long courtCenterId);
}
