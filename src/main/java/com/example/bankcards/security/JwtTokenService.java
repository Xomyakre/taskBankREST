package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final SecretKey jwtSigningKey;
    private final long expirationMs;

    public JwtTokenService(
            SecretKey jwtSigningKey,
            @Value("${app.security.jwt.expiration-ms:3600000}") long expirationMs
    ) {
        this.jwtSigningKey = jwtSigningKey;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        long iat = now.getEpochSecond();
        long exp = now.plusMillis(expirationMs).getEpochSecond();

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String rolesJson = user.getRoles().stream()
                .map(r -> "\"" + r.getName().name() + "\"")
                .collect(Collectors.joining(","));

        String payloadJson = "{\"sub\":\"" + escapeJson(user.getUsername()) + "\""
                + ",\"uid\":\"" + user.getId() + "\""
                + ",\"roles\":[" + rolesJson + "]"
                + ",\"iat\":" + iat
                + ",\"exp\":" + exp
                + "}";

        String encodedHeader = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = encodedHeader + "." + encodedPayload;

        byte[] signature = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8));
        String encodedSignature = base64Url(signature);

        return signingInput + "." + encodedSignature;
    }

    public UUID extractUserId(org.springframework.security.oauth2.jwt.Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        return UUID.fromString(uid);
    }

    private byte[] hmacSha256(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(jwtSigningKey);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("JWT signing failed", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

