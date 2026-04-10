package com.thv.sport.system.controller;

import com.nimbusds.jose.JOSEException;
import com.thv.sport.system.config.security.CustomJwtDecoder;
import com.thv.sport.system.dto.ForgotPasswordDTO;
import com.thv.sport.system.dto.request.authentication.AuthenticationRequest;
import com.thv.sport.system.dto.request.authentication.ChangePasswordInForgotRequest;
import com.thv.sport.system.dto.request.authentication.IntrospectRequest;
import com.thv.sport.system.dto.request.authentication.RefreshRequest;
import com.thv.sport.system.dto.request.user.UserCreationRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.authentication.AuthenticationResponse;
import com.thv.sport.system.dto.response.authentication.IntrospectResponse;
import com.thv.sport.system.dto.response.authentication.UserLoginResponse;
import com.thv.sport.system.exception.ErrorCode;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.AuthenticationServiceImpl;
import com.thv.sport.system.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.text.ParseException;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationServiceImpl authenticationService;
    UserServiceImpl userService;
    CustomJwtDecoder customJwtDecoder;
    UserRepository userRepository;

    //truyền tài khoản mật khẩu vào sẽ trả về token (jwt)
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) throws Exception {
        return authenticationService.authenticate(request);
    }

    //truyền token vào sẽ trả về valid true or false
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody UserCreationRequest request
    ) {
        return userService.registerAccount(request);
    }

    //khách hàng ấn vào confirm trong email sẽ được chuyển đến api này
    //và thực hiện xác nhận
    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmAccount(
            @RequestParam("token") String token
    ) {
        String result = Objects.requireNonNull(userService.confirmUser(token).getBody()).getMessage();

        String redirectUrl;

        if ("Failed".equalsIgnoreCase(result)) {
            redirectUrl = "https://your-frontend.com/error?reason=confirm_failed";
        } else {
            redirectUrl = "http://localhost:5173/account/login";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND); // HTTP 302 Redirect
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(
            @RequestBody @Valid AuthenticationRequest request
    ) throws Exception {
        try {
            //tạo token từ request
            AuthenticationResponse response = Objects.requireNonNull(authenticationService.authenticate(request)
                    .getBody()).getResult();

            //lấy jwt
            String jwt = response.getToken();

            Jwt decodedJwt = customJwtDecoder.decode(jwt);
            String role = decodedJwt.getClaimAsString("role");

            Long userId = Long.valueOf(decodedJwt.getClaimAsString("userId"));

            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception(ErrorCode.USER_NOT_EXISTED.getMessage()));

            new UserLoginResponse();
            UserLoginResponse user = UserLoginResponse.builder()
                    .email(u.getEmail())
                    .id(u.getUserId())
                    .fullName(u.getFullName())
                    .token(jwt)
                    .role(role)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .body(
                            ApiResponse.<UserLoginResponse>builder()
                                    .message("Login successfully by: " + user.getFullName())
                                    .result(user)
                                    .build()
                    );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<UserLoginResponse>builder()
                                    .message(ErrorCode.ACCOUNT_PASSWORD_NOT_CORRECT.getMessage())
                                    .build()
                    );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO
    ) {
        return authenticationService.forgotPassWord(forgotPasswordDTO);
    }

    @GetMapping("/confirm-forgot")
    public ResponseEntity<Void> confirmForgotPassword(
            @RequestParam("token") String token
    ) {
        log.debug("confirmForgotPassword");
        String result = Objects.requireNonNull(authenticationService.confirmPasswordReset(token)
                .getBody()).getMessage();

        String redirectUrl;

        if (ErrorCode.INVALID_TOKEN.getMessage().equalsIgnoreCase(result)) {
            log.info("In failed");
            redirectUrl = "https://localhost:5173/error?reason=confirm_failed";
        } else {
            log.info("Redirect to forgot password success");
            redirectUrl = "http://localhost:5173/reset-password/?token=" + token;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<User>> changePassword(
            @RequestParam("token") String token,
            @RequestBody @Valid ChangePasswordInForgotRequest request
    ) {
        return authenticationService.changePasswordFormForgot(token, request);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        return authenticationService.logOut(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody RefreshRequest request)
            throws Exception {
        return authenticationService.refreshToken(request);
    }
}
