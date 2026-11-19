package com.example.demo.auth;

import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    @Value("${auth.mode}")
    private String authMode;

    public String issueToken(User user) {
        if("session".equals(authMode)){
            return sessionService.createSession(user.getId());
        }
        return jwtUtil.generateToken(user);
    }

    public Long parseToken(String token) throws Exception {
        if("session".equals(authMode)){
            return sessionService.validate(token);
        }
        return jwtUtil.parseToken(token);
    }

    public void revokeToken(Long userId) {
        if("session".equals(authMode)){
            sessionService.revoke(userId);
        }
    }

}
