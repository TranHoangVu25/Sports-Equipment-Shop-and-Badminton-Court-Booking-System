package com.thv.sport.system.service;


import com.thv.sport.system.dto.request.user.UserCreationRequest;
import com.thv.sport.system.dto.request.user.UserUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.user.UserResponse;
import com.thv.sport.system.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ResponseEntity<ApiResponse<List<User>>> getAllUser ();

    ResponseEntity<ApiResponse<String>> registerAccount(UserCreationRequest request);

    ResponseEntity<ApiResponse<User>> updateUser(UserUpdateRequest request, Long id);

    ResponseEntity<ApiResponse<?>> deleteUser(Long id);

    ResponseEntity<ApiResponse<String>> confirmUser(String token);

    ResponseEntity<ApiResponse<User>> createUserAdminRole(UserCreationRequest request);

    ResponseEntity<ApiResponse<String>> lockUserAdminRole(Long userId);

    ResponseEntity<ApiResponse<UserResponse>> getUserById(Long userId);

    ResponseEntity<ApiResponse<UserResponse>> getUserProfile(Long userId);

    ResponseEntity<ApiResponse<List<User>>> findByRole(String role);
}
