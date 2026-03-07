package com.thv.sport.system.config.security;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // verify chữ ký HMAC HS256
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new JwtException("JWT signature verification failed");
            }

            // check expiry
//            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
//            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new java.util.Date())) {
//                throw new JwtException("JWT token expired");
//            }

            // tạo NimbusJwtDecoder (lazy init)
            if (Objects.isNull(nimbusJwtDecoder)) {
                SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS256");
                nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                        .build();
            }

            // decode bằng NimbusJwtDecoder để trả về Jwt cho Spring Security
            Jwt jwt = nimbusJwtDecoder.decode(token);
            log.info("JWT token: {}", token);
            log.info("JWT headers: {}", jwt.getHeaders());
            log.info("JWT claims: {}", jwt.getClaims());
            log.info("JWT subject: {}", jwt.getSubject());
            log.info("JWT issuedAt: {}", jwt.getIssuedAt());
            log.info("JWT expiresAt: {}", jwt.getExpiresAt());

// Nếu muốn log roles/authorities nếu bạn map từ claims
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                log.info("JWT roles: {}", roles);
            }
            return jwt;
        } catch (Exception e) {
            throw new JwtException("JWT decode failed: " + e.getMessage(), e);
        }
    }
}
