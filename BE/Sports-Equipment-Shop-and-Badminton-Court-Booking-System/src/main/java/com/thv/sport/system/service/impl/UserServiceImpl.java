package com.thv.sport.system.service.impl;


import com.thv.sport.system.dto.request.user.UserCreationRequest;
import com.thv.sport.system.dto.request.user.UserUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.user.UserResponse;
import com.thv.sport.system.exception.ErrorCode;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.UserService;
import com.thv.sport.system.util.SendEmail;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    SendEmail sendEmail;

    @Override
    public ResponseEntity<ApiResponse<List<User>>> getAllUser() {
        try {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<List<User>>builder()
                                    .message("No users found")
                                    .build()
                    );
        }
        return ResponseEntity.ok()
                .body(
                        ApiResponse.<List<User>>builder()
                                .message("Successfully retrieved users")
                                .result(users)
                                .build()
                );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<List<User>>builder()
                                    .message("Have error: " + e.getMessage())
                                    .build());
        }
    }

    //service đăng ký user
    @Override
    public ResponseEntity<ApiResponse<String>> registerAccount(UserCreationRequest request) {
        try {
        //kiểm tra user đã tồn tại ch = email
        if (userRepository.existsByEmail(request.getEmail())){
            User user_existing = userRepository.findByEmail(request.getEmail())
                    .orElse(null);

            //nếu user đã tồn tại nhưng chưa được confirm thì resend email
            if (user_existing.getConfirmedAt() == null) {
                String confirm_token = UUID.randomUUID().toString();

                user_existing.setConfirmationToken(confirm_token);
                user_existing.setConfirmationSentAt(LocalDateTime.now());

                userRepository.save(user_existing);

                sendEmail.sendEmailRegister(confirm_token,request.getEmail());

                return ResponseEntity.ok()
                        .body(
                                ApiResponse.<String>builder()
                                        .message("Resend successfully!")
                                        .build()
                        );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<String>builder()
                                    .code(ErrorCode.USER_EXISTED.getCode())
                                    .message(ErrorCode.USER_EXISTED.getMessage())
                                    .build()
                    );
        }
        String confirmToken = UUID.randomUUID().toString();
        String jit = UUID.randomUUID().toString();

        //mã hóa mật khẩu
        String password = passwordEncoder.encode(request.getEncryptedPassword());

        User user = new User().builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .jti(jit)
                .encryptedPassword(password)
                .confirmationToken(confirmToken)
                .confirmationSentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(request.getRole())
                .dob(String.valueOf(request.getDob()))
                .build();

        userRepository.save(user);

        //method gửi email
        sendEmail.sendEmailRegister(confirmToken,user.getEmail());

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Verification email sent. Please check your inbox.")
                        .build()
        );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Register have error: " + e.getMessage())
                                    .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<User>> updateUser(UserUpdateRequest request, Long userId) {
        try {
        //Trường hợp userId không tồn tại
        if (!userRepository.existsById(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<User>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }

        User user = userRepository.findById(userId).get();
        //nếu k có thay đổi về mật khẩu thì dùng mật khẩu cũ
        if(request.getEncryptedPassword()==null){
            user.setEncryptedPassword(user.getEncryptedPassword());
        }else {
            String password = passwordEncoder.encode(request.getEncryptedPassword());
            user.setEncryptedPassword(password);
        }

        user.setFullName(request.getFullName());
        user.setUpdatedAt(LocalDateTime.now());

        //lưu thông tin thay đổi vào db
        User saved_user = userRepository.save(user);

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<User>builder()
                                .message("Update user successfully")
                                .result(saved_user)
                                .build()
                );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<User>builder()
                                    .message("Update have error: " + e.getMessage())
                                    .build());
        }
    }

    //xóa user và gửi event qua promotion
    @Override
    public ResponseEntity<ApiResponse<?>> deleteUser(Long userId) {
        try {
        if (!userRepository.existsById(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<User>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }
        userRepository.deleteById(userId);

        return ResponseEntity.ok()
                .body(
                        ApiResponse.builder()
                                .message("Delete user successfully")
                                .build()
                );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Delete user have error: " + e.getMessage())
                                    .build());
        }
    }

    //hàm xác nhận user khi chọn xác nhận trong mail
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> confirmUser(String token) {
        try {
        User user = userRepository.findByConfirmationToken(token)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>builder()
                            .code(ErrorCode.INVALID_TOKEN.getCode())
                            .message("Failed")
                            .build());
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setConfirmationToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        user.setConfirmedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Confirm successfully!")
                        .build()
        );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Cofirm email have error: " + e.getMessage())
                                    .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<User>> createUserAdminRole(UserCreationRequest request) {
        try {
            //kiểm tra user đã tồn tại ch = email
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.<User>builder()
                                        .code(ErrorCode.USER_EXISTED.getCode())
                                        .message(ErrorCode.USER_EXISTED.getMessage())
                                        .build()
                        );
            }
            //mã hóa mật khẩu
            String password = passwordEncoder.encode(request.getEncryptedPassword());

            User user = User.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .jti(UUID.randomUUID().toString())
                    .encryptedPassword(password)
                    .confirmedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .role(request.getRole())
                    .dob(String.valueOf(request.getDob()))
                    .build();

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(
                    ApiResponse.<User>builder()
                            .message("Create user successfully.")
                            .result(savedUser)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<User>builder()
                                    .message("Create user have error "+e.getMessage())
                                    .build()
                    );
        }
    }

    @Override
    public ResponseEntity<ApiResponse<String>> lockUserAdminRole(Long userId) {
        try {
        //Trường hợp userId không tồn tại
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<String>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }
        User user = userRepository.findById(userId).get();

        user.setLockedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<String>builder()
                                .message("Locked user have id: " + userId)
                                .build()
                );
        }
        catch (Exception e) {
            log.error(e.getMessage());
            {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .message("Lock user have error: " + e.getMessage())
                                        .build());
            }
        }
    }

    //lấy thoong tin user trong dashboard
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(Long userId) {
        if (!userRepository.existsById(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<UserResponse>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }
        User user = userRepository.findById(userId).get();

        UserResponse userResponse = UserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .lockedAt(user.getLockedAt())
                .createdAt(user.getCreatedAt())
                .lastSignInAt(user.getLastSignInAt())
                .id(user.getUserId())
                .addresses(user.getAddresses())
                .build();
        return ResponseEntity.ok()
                .body(
                        ApiResponse.<UserResponse>builder()
                                .result(userResponse)
                                .build()
                );
    }

    // lấy profile user trong màn user
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<UserResponse>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }
        User user = userRepository.findById(userId).get();

        UserResponse userResponse = new UserResponse().builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
//                .lineUserId(user.getLineUserId())
                .build();

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<UserResponse>builder()
                                .message("Get profile user has id: " + userId)
                                .result(userResponse)
                                .build()
                );
    }

    @Override
    public ResponseEntity<ApiResponse<List<User>>> findByRole(String role) {
        return null;
    }

//    @Override
//    public ResponseEntity<ApiResponse<List<User>>> findByRole(String role) {
//        List<User> users = userRepository.findByRole(role);
//        if (users.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok()
//                .body(
//                        ApiResponse.<List<User>>builder()
//                                .result(users)
//                                .build()
//                );
//    }
}
