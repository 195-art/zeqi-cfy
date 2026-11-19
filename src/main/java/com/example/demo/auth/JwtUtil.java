package com.example.demo.auth;

import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;   // 毫秒

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        long exp = now + expiration;

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"userId\":" + user.getId()
                + ",\"username\":\"" + user.getUsername()
                + "\",\"iat\":" + now
                + ",\"exp\":" + exp + "}";

        String encodedHeader = base64UrlEncode(header);
        String encodedPayload = base64UrlEncode(payload);
        String toSign = encodedHeader + "." + encodedPayload;
        String signature = hmacSha256(toSign, secret);

        return toSign + "." + signature;
    }

    public Long parseToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "无效的Token");
        }

        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String signature = parts[2];

        String expected = hmacSha256(encodedHeader + "." + encodedPayload, secret);
        if (!expected.equals(signature)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "伪造的Token");
        }

        String payloadJson = base64UrlDecode(encodedPayload);
        Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

        long exp = ((Number) claims.get("exp")).longValue();
        if (System.currentTimeMillis() > exp) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        return ((Number) claims.get("userId")).longValue();
    }

    private String base64UrlEncode(String str) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlDecode(String str) {
        return new String(Base64.getUrlDecoder().decode(str), StandardCharsets.UTF_8);
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("JWT签名失败", e);
        }
    }
}
