package com.thv.sport.system.service;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.thv.sport.system.dto.ForgotPasswordDTO;
import com.thv.sport.system.dto.request.authentication.AuthenticationRequest;
import com.thv.sport.system.dto.request.authentication.ChangePasswordInForgotRequest;
import com.thv.sport.system.dto.request.authentication.ChangePasswordRequest;
import com.thv.sport.system.dto.request.authentication.IntrospectRequest;
import com.thv.sport.system.dto.request.authentication.RefreshRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.authentication.AuthenticationResponse;
import com.thv.sport.system.dto.response.authentication.IntrospectResponse;
import com.thv.sport.system.exception.ErrorCode;
import com.thv.sport.system.model.InvalidatedToken;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.InvalidatedTokenRepository;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.util.GenerateRandomPassword;
import com.thv.sport.system.util.SendEmail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl {
    UserRepository userRepository;
    SendEmail sendEmail;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal //không bị inject contructor
    @Value("${jwt.signerKey}") //anotation này được sử dụng để đọc biến trong file .yaml
    //https://generate-random.org/
    protected String signKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long refreshableDuration;

    GenerateRandomPassword generateRandomPassword;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        //thêm khối try catch để nếu verifyToken trả về exception thì trả về false
        try {
            verifyToken(token, false);

        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(AuthenticationRequest request)
            throws Exception {
        try {
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new Exception(ErrorCode.USER_NOT_EXISTED.getMessage()));

            log.info("User confirmed:" + user.getConfirmedAt());

            //nếu tài khoản chưa được confirm hoặc đã bị khóa thì k truy cập được
            if (user.getConfirmedAt() == null) {
//            log.error("Error not confirm email: "+ErrorCode.EMAIL_NOT_CONFIRMED.getMessage());
                throw new RuntimeException(new Exception(ErrorCode.EMAIL_NOT_CONFIRMED.getMessage()));
            }
            // check boolean flag isLocked instead of lockedAt
            if (user.isLocked()) {
                throw new RuntimeException(new Exception(ErrorCode.ACCOUNT_WAS_LOCKED.getMessage()));
            }
            //mã hóa mật khẩu user nhập
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String rawPassword = request.getPassword().trim();

            //so sánh mật khẩu mã hóa và mật khẩu trong db
            boolean authenticated = passwordEncoder.matches(rawPassword, user.getEncryptedPassword());

            if (!authenticated) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.<AuthenticationResponse>builder()
                                        .message(ErrorCode.ACCOUNT_PASSWORD_NOT_CORRECT.getMessage())
                                        .build()
                        );
            }

            var token = generateToken(user);

            AuthenticationResponse authenticationResponse = new AuthenticationResponse(token, authenticated);

            if (!authenticationResponse.isAuthenticated()) {
                throw new RuntimeException(new Exception(ErrorCode.ACCOUNT_PASSWORD_NOT_CORRECT.getMessage()));
            }
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<AuthenticationResponse>builder()
                                    .result(authenticationResponse)
                                    .message("Get token successful")
                                    .build()
                    );

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Authentication Exception{}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<AuthenticationResponse>builder()
                                    .message("Authentication Exception" + e.getMessage())
                                    .build()
                    );
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws Exception {
        //Tạo đối tượng để xác minh chữ ký JWT bằng khóa bí mật SIGN_KEY
        JWSVerifier verifier = new MACVerifier(signKey.getBytes());

        //Phân tích chuỗi token thành đối tượng SignedJWT để truy xuất header, payload và chữ ký
        SignedJWT signedJWT = SignedJWT.parse(token);

        // lấy ngày hết hạn của token
        Date expiryTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                        .plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        //kiểm tra token hợp lệ
        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) // time hết hạn sau time hiện tại
        {
            log.info("Token was expired");
            throw (new Exception(String.valueOf(ErrorCode.UNAUTHENTICATED)));
        }
//
//        //kiểm tra xem nếu token đã tồn tại trong bảng InvalidatedToken thì trả về lỗi
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            log.info("jwt id was existed in InvalidatedToken table");
            throw (new Exception(ErrorCode.UNAUTHENTICATED.getMessage()));
        }
        return signedJWT;
    }

    public String generateToken(User user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("THV.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()
                ))

                .claim("scope", buildScope(user)).claim("role", buildScope(user))
                .claim("userId", user.getUserId()).claim("full_name", user.getFullName())
                //#16 thêm vào ID của jwt để lưu trữ token gần nhất mới hết hạn trong db
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signKey.getBytes()));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token ", e);
            throw new RuntimeException(e);
        }
    }

    //hàm thêm scope(role) vào trong jwt
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {
            stringJoiner.add("ROLE_" + user.getRole());
        }
        return stringJoiner.toString();
    }

    public boolean checkPassword(String rawPassword, String hashPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, hashPassword);
    }

    @SuppressWarnings("checkstyle:LocalVariableName")
    public ResponseEntity<ApiResponse<String>> forgotPassWord(ForgotPasswordDTO forgotPasswordDTO) {
        try {
            if (!userRepository.existsByEmail(forgotPasswordDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                        .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                        .build()
                        );
            }

            User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage()));

            //nếu user đăng ký mà chưa xác nhận email thì k dùng chức năng quên password được
            if (user.getConfirmedAt() == null) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.EMAIL_NOT_CONFIRMED.getCode())
                                        .message(ErrorCode.EMAIL_NOT_CONFIRMED.getMessage())
                                        .build()
                        );
            }
            //tạo confirm token
            String forgotToken = UUID.randomUUID().toString();
            user.setConfirmForgot(forgotToken);
            //set expire time
            user.setConfirmForgotExpired(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            //gọi method gửi email
            sendEmail.sendEmailForgotPassword(forgotToken, user.getEmail());

            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Check inbox to see reset password email!")
                                    .build()
                    );
        } catch (RuntimeException e) {
            log.error("Forgot Password Error", e);
            throw new RuntimeException(e);
        }
    }

    //kiểm tra token trong mail có hợp lệ k
    public ResponseEntity<ApiResponse<String>> confirmPasswordReset(String forgotToken) {
        try {
            if (!userRepository.existsByConfirmForgot(forgotToken)) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.INVALID_TOKEN.getCode())
                                        .message(ErrorCode.INVALID_TOKEN.getMessage())
                                        .build()
                        );
            }
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Confirm successfully")
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("Forgot Password Error", e);
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message(e.getMessage())
                                    .build()
                    );
        }
    }

    // đổi mật khẩu khi quên mật khẩu
    public ResponseEntity<ApiResponse<User>> changePasswordFormForgot
    (String token, ChangePasswordInForgotRequest request) {
        try {
            User user = userRepository.findByConfirmForgot(token)
                    .orElseThrow(() -> new RuntimeException(ErrorCode.INVALID_TOKEN.getMessage()));

            //nếu user không tồn tại hoặc thời gian xác nhận hết hạn
            if (!userRepository.existsByConfirmForgot(token)
                    || user.getConfirmForgotExpired().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<User>builder()
                                        .code(ErrorCode.INVALID_TOKEN.getCode())
                                        .message(ErrorCode.INVALID_TOKEN.getMessage())
                                        .build()
                        );
            }
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

//            String rawPassword = request.getPasswordConfirm().trim();

            //so sánh mật khẩu mã hóa và mật khẩu trong db
//            boolean authenticated = passwordEncoder.matches(rawPassword, user.getEncryptedPassword());
//
//            log.info("AUTHENTICATE:==="+authenticated);
//
//            //nếu old password khác password trong db
//            if (!authenticated) {
//                return ResponseEntity.badRequest()
//                        .body(
//                                ApiResponse.<User>builder()
//                                        .code(ErrorCode.INCORRECT_PASSWORD.getCode())
//                                        .message(ErrorCode.INCORRECT_PASSWORD.getMessage())
//                                        .build()
//                        );
//            }
            String newPassword = passwordEncoder.encode(request.getNewPassword());

            user.setEncryptedPassword(newPassword);
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<User>builder()
                                    .message("Change password successfully")
                                    .result(savedUser)
                                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<User>builder()
                                    .message(e.getMessage())
                                    .build());
        }
    }

    //đổi mk trong user setting
    public ResponseEntity<ApiResponse<String>> changePassword(Long userId, ChangePasswordRequest request) {
        try {
            //nếu user không tồn tại hoặc thời gian xác nhận hết hạn
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                        .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                        .build()
                        );
            }
            User user = userRepository.findById(userId).orElse(null);
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String rawPassword = request.getOldPassword().trim();

            //so sánh mật khẩu mã hóa và mật khẩu trong db
            boolean authenticated = passwordEncoder.matches(rawPassword, user.getEncryptedPassword());

            log.info("AUTHENTICATE:===" + authenticated);

            //nếu old password khác password trong db
            if (!authenticated) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.<String>builder()
                                        .code(ErrorCode.INCORRECT_PASSWORD.getCode())
                                        .message(ErrorCode.INCORRECT_PASSWORD.getMessage())
                                        .build()
                        );
            }
            String newPassword = passwordEncoder.encode(request.getNewPassword());

            user.setEncryptedPassword(newPassword);
            userRepository.save(user);
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Change password successfully")
                                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<String>builder()
                                    .message("Have error" + e.getMessage())
                                    .build());
        }
    }

    public ResponseEntity<ApiResponse<String>> logOut(HttpServletRequest request) {

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // logout idempotent
                return ResponseEntity.ok(
                        ApiResponse.<String>builder()
                                .message("Logout successfully")
                                .build()
                );
            }

            String token = authHeader.substring(7);

            // verify chữ ký, KHÔNG check expiry
            SignedJWT signedToken = verifyToken(token, false);

            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            if (jti != null) {
                invalidatedTokenRepository.save(
                        InvalidatedToken.builder()
                                .id(jti)
                                .expiryTime(expiryTime)
                                .build()
                );
            }

        } catch (Exception e) {
            log.info("Logout ignored: {}", e.getMessage());
        }

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Logout successfully")
                        .build()
        );
    }

    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(RefreshRequest request) throws Exception {
        try {
            var signedJWT = verifyToken(request.getToken(), true);

            var jit = signedJWT.getJWTClaimsSet().getJWTID();
            var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);

            var email = signedJWT.getJWTClaimsSet().getSubject();

            var user = userRepository.findByEmail(email).orElseThrow(
                    () -> new RuntimeException(ErrorCode.UNAUTHENTICATED.getMessage())
            );
            var token = generateToken(user);

            AuthenticationResponse auth = AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<AuthenticationResponse>builder()
                                    .message("Get token successfully")
                                    .result(auth)
                                    .build()
                    );
        } catch (Exception e) {
            log.info("Logout failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.<AuthenticationResponse>builder()
                                    .message("Logout failed: " + e.getMessage())
                                    .build()
                    );
        }
    }
}

