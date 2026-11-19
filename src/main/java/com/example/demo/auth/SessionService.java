package com.example.demo.auth;

import com.example.demo.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SessionService {

    private final ConcurrentMap<String,Long> sessionMap = new ConcurrentHashMap<>();

    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, userId);
        return sessionId;
    }

    public Long validate(String sessionId) {
        Long userId = sessionMap.get(sessionId);
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,"未登录或登录已失效");
        }
        return userId;
    }

    public void revoke(Long userId) {
        sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(userId));
    }

}
