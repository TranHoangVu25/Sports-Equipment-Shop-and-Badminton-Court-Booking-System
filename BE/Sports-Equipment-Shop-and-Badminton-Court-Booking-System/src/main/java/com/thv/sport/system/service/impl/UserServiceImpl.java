package com.thv.sport.system.service.impl;


import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.request.user.UserCreationRequest;
import com.thv.sport.system.dto.request.user.UserUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.user.UserResponse;
import com.thv.sport.system.dto.response.user.UserStatsResponse;
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
import java.util.ArrayList;
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
    com.thv.sport.system.service.OrderService orderService;
    com.thv.sport.system.respository.OrderRepository orderRepository;


    @SuppressWarnings("checkstyle:WhitespaceAround")
    @Override
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUser(String userName) {
        try {
            List<UserResponse> userResponses = new ArrayList<>();
            List<User> users;

            users = userRepository.findAllByUserNameLike(userName);

            for (User u : users) {
                UserResponse response = UserResponse.builder()
                        .id(u.getUserId())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .fullName(u.getFullName())
                        .phoneNumber(u.getPhoneNumber())
                        .location(u.getLocation())
                        .gender(u.getGender())
                        .dob(u.getDob())
                        .lockedAt(u.getLockedAt())
                        .isLocked(u.isLocked())
                        .createdAt(u.getCreatedAt())
                        .lastSignInAt(u.getLastSignInAt())
                        .build();
                userResponses.add(response);
            }
            if (users.isEmpty()) {
                return ResponseEntity.ok()
                        .body(
                                ApiResponse.<List<UserResponse>>builder()
                                        .message("No users found")
                                        .build()
                        );
            }
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<List<UserResponse>>builder()
                                    .message("Successfully retrieved users")
                                    .result(userResponses)
                                    .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<List<UserResponse>>builder()
                                    .message("Have error: " + e.getMessage())
                                    .build());
        }
    }

    //service đăng ký user
    @Override
    public ResponseEntity<ApiResponse<String>> registerAccount(UserCreationRequest request) {
        try {
            //kiểm tra user đã tồn tại ch = email
            if (userRepository.existsByEmail(request.getEmail())) {
                User userExisting = userRepository.findByEmail(request.getEmail())
                        .orElse(null);

                //nếu user đã tồn tại nhưng chưa được confirm thì resend email
                if (userExisting.getConfirmedAt() == null) {
                    String confirmToken = UUID.randomUUID().toString();

                    userExisting.setConfirmationToken(confirmToken);
                    userExisting.setConfirmationSentAt(LocalDateTime.now());

                    userRepository.save(userExisting);

                    sendEmail.sendEmailRegister(confirmToken, request.getEmail());

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

            User user = User.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .jti(jit)
                    .encryptedPassword(password)
                    .confirmationToken(confirmToken)
                    .confirmationSentAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .role(Constants.Role.USER)
                    .phoneNumber(request.getPhoneNumber())
                    .dob(request.getDob())
                    .build();

            userRepository.save(user);

            //method gửi email
            sendEmail.sendEmailRegister(confirmToken, user.getEmail());

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
    public ResponseEntity<ApiResponse<User>> updateUserWithAdminRole(UserUpdateRequest request, Long userId) {
        try {
            //Trường hợp userId không tồn tại
            if (!userRepository.existsById(userId)) {
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
            if (request.getEncryptedPassword() == null) {
                user.setEncryptedPassword(user.getEncryptedPassword());
            } else {
                String password = passwordEncoder.encode(request.getEncryptedPassword());
                user.setEncryptedPassword(password);
            }

            user.setFullName(request.getFullName());
            user.setUpdatedAt(LocalDateTime.now());

            //lưu thông tin thay đổi vào db
            User savedUser = userRepository.save(user);

            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<User>builder()
                                    .message("Update user successfully")
                                    .result(savedUser)
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
            if (!userRepository.existsById(userId)) {
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
                    .isLocked(Constants.TrueFalseValue.FALSE)
                    .role(request.getRole())
                    .dob(request.getDob())
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
                                    .message("Create user have error " + e.getMessage())
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
            User user = userRepository.findById(userId).orElseThrow();

            user.setLockedAt(LocalDateTime.now());
            user.setLocked(Constants.TrueFalseValue.TRUE);

            userRepository.save(user);

            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Locked user have id: " + userId)
                                    .build()
                    );
        } catch (Exception e) {
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

    @Override
    public ResponseEntity<ApiResponse<String>> unlockUserAdminRole(Long userId) {
        try {
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                        .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                        .build()
                        );
            }

            User user = userRepository.findById(userId).orElseThrow();

            user.setLocked(Constants.TrueFalseValue.FALSE);
            user.setLockedAt(null);

            userRepository.save(user);

            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Unlocked user have id: " + userId)
                                    .build()
                    );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Unlock user have error: " + e.getMessage())
                                    .build());
        }
    }

    //lấy thoong tin user trong dashboard
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(Long userId) {
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

        UserResponse userResponse = UserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .lockedAt(user.getLockedAt())
                .createdAt(user.getCreatedAt())
                .lastSignInAt(user.getLastSignInAt())
                .id(user.getUserId())
                .location(user.getLocation())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dob(user.getDob())
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

        UserResponse userResponse = UserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
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

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> changeProfile(UserUpdateRequest request, Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<UserResponse>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(userId).orElseThrow();
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setUpdatedAt(now);
        user.setLocation(request.getLocation());

        userRepository.save(user);

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<UserResponse>builder()
                                .result(UserResponse.builder()
                                        .id(user.getUserId())
                                        .email(user.getEmail())
                                        .fullName(user.getFullName())
                                        .phoneNumber(user.getPhoneNumber())
                                        .location(user.getLocation())
                                        .gender(user.getGender())
                                        .dob(user.getDob())
                                        .build())
                                .build()
                );
    }

    @Override
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(int days) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days <= 0 ? 2 : days);
            UserStatsResponse stats = userRepository.getUserStatsSince(since);

            return ResponseEntity.ok(
                    ApiResponse.<UserStatsResponse>builder()
                            .result(stats)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting user stats", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<UserStatsResponse>builder()
                                    .message("Get user stats have error: " + e.getMessage())
                                    .build()
                    );
        }
    }

    @Override
    public ResponseEntity<ApiResponse<com.thv.sport.system.dto.response.user.UserDetailResponse>> getUserDetail(Long userId) {
        try {
            // 1. Get user info (reuse existing logic)
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<com.thv.sport.system.dto.response.user.UserDetailResponse>builder()
                                .code(com.thv.sport.system.exception.ErrorCode.USER_NOT_EXISTED.getCode())
                                .message(com.thv.sport.system.exception.ErrorCode.USER_NOT_EXISTED.getMessage())
                                .build());
            }

            ResponseEntity<ApiResponse<com.thv.sport.system.dto.response.user.UserResponse>> userRespEntity = this.getUserById(userId);
            com.thv.sport.system.dto.response.user.UserResponse userInfo = null;
            if (userRespEntity != null && userRespEntity.getBody() != null) {
                userInfo = userRespEntity.getBody().getResult();
            }

            // 2. Get latest 3 orders for user by calling orderService
            ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.thv.sport.system.dto.response.order.OrderResponse>>> ordersResp =
                    orderService.getAllOrders(userId, 0, 3, false, null);

            java.util.List<com.thv.sport.system.dto.response.order.OrderResponse> orders = new java.util.ArrayList<>();
            double totalAmount = 0.0;
            Integer totalOrders = 0;

            if (ordersResp != null && ordersResp.getBody() != null && ordersResp.getBody().getResult() != null) {
                org.springframework.data.domain.Page<com.thv.sport.system.dto.response.order.OrderResponse> page = ordersResp.getBody().getResult();
                orders = page.getContent();
                java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
                for (com.thv.sport.system.dto.response.order.OrderResponse or : orders) {
                    if (or.getTotalAmount() != null) {
                        sum = sum.add(or.getTotalAmount());
                    }
                }
                totalAmount = sum.doubleValue();
                // total number of orders for this user (from paged result)
                totalOrders = orderRepository.getNumberOfOrdersByUserId(userId);
            }

            com.thv.sport.system.dto.response.user.UserDetailResponse detail =
                    com.thv.sport.system.dto.response.user.UserDetailResponse.builder()
                            .userInfo(userInfo)
                            .orders(orders)
                            .totalAmount(totalAmount)
                            .totalOrders(totalOrders)
                            .build();

            return ResponseEntity.ok(
                    ApiResponse.<com.thv.sport.system.dto.response.user.UserDetailResponse>builder()
                                    .message("Get user detail successfully")
                                            .result(detail)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting user detail", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<com.thv.sport.system.dto.response.user.UserDetailResponse>builder()
                            .message("Get user detail have error: " + e.getMessage())
                            .build());
        }
    }
}
