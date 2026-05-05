package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.dto.response.BaseResponse;
import com.thv.sport.system.dto.response.courtcenter.CourtCenterResponse;
import com.thv.sport.system.model.CourtCenter;
import com.thv.sport.system.service.CourtCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping(Constants.ApiPath.API_COURT_CENTER)
@Slf4j
public class CourtCenterController extends BaseController {
    CourtCenterService courtCenterService;

    @PreAuthorize("hasAuthority(T(com.thv.sport.system.enums.UserRole).ADMIN.value)")
    @PostMapping("/create")
    @Operation(
            summary = "Create court center",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Update template successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user is not authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<BaseResponse<CourtCenter>> getHomeProducts(
            @RequestBody @Valid CourtCenterRegisterRequest request
    ) {
        CourtCenter response = courtCenterService.registerCourt(request);
        return successResponse(response, "court.center.create.success", null);
    }

    @GetMapping("/detail/{courtCenterId}")
    @Operation(
            summary = "Create court center",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Update template successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user is not authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<BaseResponse<CourtCenter>> getHomeProducts(
            @PathVariable Long courtCenterId
    ) {
        CourtCenter response = courtCenterService.getCourtDetails(courtCenterId);
        return successResponse(response, "common.success", null);
    }

    @PreAuthorize("hasAuthority(T(com.thv.sport.system.enums.UserRole).ADMIN.value)")
    @PutMapping("/update/{courtCenterId}")
    @Operation(
            summary = "Update court center",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Update court successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user is not authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<BaseResponse<CourtCenter>> getHomeProducts(
            @RequestBody @Valid CourtCenterRegisterRequest request,
            @PathVariable Long courtCenterId
    ) {
        CourtCenter response = courtCenterService.updateCourt(request, courtCenterId);
        return successResponse(response, "court.center.update.success", null);
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<Page<CourtCenterResponse>>> getHomeProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
try{

        Pageable pageable = PageRequest.of(page, size);

        Page<CourtCenterResponse> response =
                courtCenterService.search(name,  userLat, userLng, pageable);

        return successResponse(response, "common.success", null);
    } catch (Exception e) {
    throw new RuntimeException(e);
}
    }

}
