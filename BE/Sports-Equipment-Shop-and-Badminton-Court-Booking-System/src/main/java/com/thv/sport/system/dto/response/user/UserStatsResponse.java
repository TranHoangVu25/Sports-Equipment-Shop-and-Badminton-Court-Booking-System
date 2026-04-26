package com.thv.sport.system.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO to carry aggregated user statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {
    private  long totalUsers;
    private  long newUsers;
    private  long lockedUsers;

}

