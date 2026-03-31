package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.model.Court;

public interface CourtService {
    Court registerCourt(CourtCenterRegisterRequest request);
}
