package com.thv.sport.system.dto.response.user;

import com.thv.sport.system.model.Address;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String email;
    String fullName;
    String role;
    String lineUserId;
    LocalDateTime lockedAt;
    LocalDateTime createdAt;
    LocalDateTime lastSignInAt;
    List<Address> addresses;
}
