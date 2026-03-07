package com.thv.sport.system.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String email = "";
    private String fullName = "";
    private String encryptedPassword = "";
    private LocalDateTime updatedAt;
    private Integer signInCount;
    private LocalDateTime currentSignInAt;
    private LocalDate dob;
}
