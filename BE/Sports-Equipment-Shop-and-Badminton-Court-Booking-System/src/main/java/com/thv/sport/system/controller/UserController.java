package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.config.UserContextHolder;
import com.thv.sport.system.dto.request.authentication.ChangePasswordRequest;
import com.thv.sport.system.dto.request.user.UserCreationRequest;
import com.thv.sport.system.dto.request.user.UserUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.user.UserResponse;
import com.thv.sport.system.model.User;
import com.thv.sport.system.service.AuthenticationServiceImpl;
import com.thv.sport.system.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.ApiPath.API_USER)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    AuthenticationServiceImpl authenticationService;

    @PostMapping()
    public ResponseEntity<ApiResponse<String>> registerAccount(
            @RequestBody @Valid UserCreationRequest request
    ) {
        return userService.registerAccount(request);
    }

    //xem tất cả các user để test
    @GetMapping()
    public ResponseEntity<ApiResponse<List<User>>> getAllUser(){
        return userService.getAllUser();
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @RequestBody @Valid UserUpdateRequest request,
            @PathVariable Long userId
    ){
        return userService.updateUser(request,userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @PathVariable Long userId
    ){
        return userService.deleteUser(userId);
    }

    @PostMapping("/lock/{userId}")
    public ResponseEntity<ApiResponse<String>> lockUser(
            @PathVariable Long userId
    ) {
        return userService.lockUserAdminRole(userId);
    }

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<User>> createAccount(
            @RequestBody @Valid UserCreationRequest request
    ) {
        return userService.createUserAdminRole(request);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long userId
    ){
        return userService.getUserById(userId);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
    ){
        Long userId = UserContextHolder.getUserId();
        return userService.getUserById(userId);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request
    ){
        Long userId = UserContextHolder .getUserId();
        return authenticationService.changePassword(userId,request);
    }
}
